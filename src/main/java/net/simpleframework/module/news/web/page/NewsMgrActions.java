package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.web.page.AbstractDescPage;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsRecommend;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsLogRef.NewsUpdateLogPage;
import net.simpleframework.module.news.web.NewsUrlsFactory;
import net.simpleframework.module.news.web.page.t1.NewsCommentPage;
import net.simpleframework.module.news.web.page.t1.NewsFormBasePage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.CalendarInput;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.ajaxrequest.DefaultAjaxRequestHandler;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsMgrActions extends DefaultAjaxRequestHandler implements INewsContextAware {

	public static void addMgrComponentBean(final PageParameter pp,
			final Class<? extends NewsMgrActions> mgrActionsClass,
			final Class<? extends StatusDescPage> statusDescClass) {
		// edit
		pp.addComponentBean("NewsMgrPage_edit", AjaxRequestBean.class).setHandlerMethod("doEdit")
				.setHandlerClass(mgrActionsClass);
		// delete
		pp.addComponentBean("NewsMgrPage_delete", AjaxRequestBean.class)
				.setConfirmMessage($m("NewsMgrPage.11")).setHandlerMethod("doDelete")
				.setHandlerClass(mgrActionsClass);
		// status
		pp.addComponentBean("NewsMgrPage_status", AjaxRequestBean.class).setHandlerMethod("doStatus")
				.setHandlerClass(mgrActionsClass);

		// status window
		AjaxRequestBean ajaxRequest = pp.addComponentBean("NewsMgrPage_statusPage",
				AjaxRequestBean.class).setUrlForward(AbstractMVCPage.url(statusDescClass));
		pp.addComponentBean("NewsMgrPage_statusWindow", WindowBean.class)
				.setContentRef(ajaxRequest.getName()).setWidth(420).setHeight(240);

		// comment window
		ajaxRequest = pp.addComponentBean("NewsMgrPage_commentPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(NewsCommentPage.class));
		pp.addComponentBean("NewsMgrPage_commentWindow", WindowBean.class)
				.setContentRef(ajaxRequest.getName()).setHeight(540).setWidth(864);

		// adv window
		ajaxRequest = pp.addComponentBean("NewsMgrPage_advPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(NewsAdvPage.class));
		pp.addComponentBean("NewsMgrPage_advWindow", WindowBean.class)
				.setContentRef(ajaxRequest.getName()).setTitle($m("NewsMgrPage.13")).setHeight(280)
				.setWidth(420);

		// 推荐
		ajaxRequest = pp.addComponentBean("NewsMgrPage_recommendationPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(RecommendPage.class));
		pp.addComponentBean("NewsMgrPage_recommendation", WindowBean.class)
				.setContentRef(ajaxRequest.getName()).setHeight(500).setWidth(740)
				.setTitle($m("AbstractContentBean.2"));

		// log window
		final IModuleRef ref = ((INewsWebContext) newsContext).getLogRef();
		if (ref != null) {
			ajaxRequest = pp.addComponentBean("NewsMgrPage_update_logPage", AjaxRequestBean.class)
					.setUrlForward(AbstractMVCPage.url(NewsUpdateLogPage.class));
			pp.addComponentBean("NewsMgrPage_update_log", WindowBean.class)
					.setContentRef(ajaxRequest.getName()).setHeight(540).setWidth(864);
		}
	}

	public IForward doEdit(final ComponentParameter cp) {
		final JavascriptForward js = new JavascriptForward();
		final News news = NewsUtils.getNews(cp);
		final EContentStatus status = news.getStatus();
		if (status == EContentStatus.edit) {
			final NewsUrlsFactory uFactory = ((INewsWebContext) newsContext).getUrlsFactory();
			js.append(JS.loc(uFactory.getUrl(cp, NewsFormBasePage.class, news)));
		} else {
			js.append("if (confirm('").append($m("NewsMgrPage.8", status))
					.append("')) { $Actions['NewsMgrPage_statusWindow']('op=")
					.append(EContentStatus.edit.name()).append("&newsId=").append(news.getId())
					.append("'); }");
		}
		return js;
	}

	@Transaction(context = INewsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("newsId"));
		_newsService.delete(ids);
		return createTableRefresh();
	}

	public IForward doStatus(final ComponentParameter cp) {
		final JavascriptForward js = new JavascriptForward();
		final EContentStatus op = cp.getEnumParameter(EContentStatus.class, "op");
		final String newsId = cp.getParameter("newsId");
		final ArrayList<String> deletes = new ArrayList<String>();
		for (final String id : StringUtils.split(newsId, ";")) {
			final News news = _newsService.getBean(id);
			final EContentStatus status = news.getStatus();
			if (op == status && op != EContentStatus.delete) {
				js.append("alert('").append($m("NewsMgrPage.9", op)).append("');");
				return js;
			}
			if (status == EContentStatus.delete) {
				deletes.add(news.getId().toString());
			}
		}
		if (op == EContentStatus.delete && deletes.size() > 0) {
			js.append("$Actions['NewsMgrPage_delete']('newsId=")
					.append(StringUtils.join(deletes, ";")).append("')");
		} else {
			js.append("$Actions['NewsMgrPage_statusWindow']('op=").append(op.name())
					.append("&newsId=").append(newsId).append("');");
		}
		return js;
	}

	protected JavascriptForward createTableRefresh() {
		return CategoryTableLCTemplatePage.createTableRefresh();
	}

	public static class StatusDescPage extends AbstractDescPage {

		@Override
		@Transaction(context = INewsContext.class)
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final EContentStatus op = cp.getEnumParameter(EContentStatus.class, "op");
			final String[] arr = StringUtils.split(cp.getParameter("newsId"), ";");
			for (final String id : arr) {
				final News news = _newsService.getBean(id);
				setLogDescription(cp, news);
				news.setStatus(op);
				_newsService.update(new String[] { "status" }, news);
			}

			final JavascriptForward js = super.onSave(cp);
			return js.append(createTableRefresh().toString());
		}

		protected JavascriptForward createTableRefresh() {
			return CategoryTableLCTemplatePage.createTableRefresh();
		}

		@Override
		public String getTitle(final PageParameter pp) {
			final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
			return $m("StatusDescLogPage.0",
					op == EContentStatus.edit ? $m("NewsMgrPage.7") : Convert.toString(op));
		}

		@Override
		protected InputElement createTextarea(final PageParameter pp) {
			final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
			return super.createTextarea(pp).setText(
					$m("StatusDescLogPage.1",
							op == EContentStatus.edit ? $m("NewsMgrPage.7") : Convert.toString(op),
							Convert.toDateString(new Date()), pp.getLogin()));
		}
	}

	public static class NewsAdvPage extends AbstractTemplatePage implements INewsContextAware {

		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addAjaxRequest(pp, "NewsAdvPage_reIndex").setConfirmMessage($m("NewsAdvPage.2"))
					.setHandlerMethod("doIndex");
		}

		@Override
		public Map<String, Object> createVariables(final PageParameter pp) {
			return ((KVMap) super.createVariables(pp)).add("LinkButton", LinkButton.class);
		}

		public IForward doIndex(final ComponentParameter cp) {
			_newsService.getLuceneService().rebuildIndex();
			return new JavascriptForward("alert('").append($m("NewsAdvPage.3")).append("');");
		}

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			final StringBuilder sb = new StringBuilder();
			sb.append("<div class='NewsAdvPage'>");
			sb.append(" <div class='cc1'><p>#(NewsAdvPage.1)</p>");
			sb.append("  <div>").append(
					LinkButton.corner($m("NewsAdvPage.0")).setOnclick(
							"$Actions['NewsAdvPage_reIndex']();"));
			sb.append("  </div>");
			sb.append(" </div>");
			sb.append(" <div class='bc'>").append(LinkButton.closeBtn().corner()).append("</div>");
			sb.append("</div>");
			return sb.toString();
		}
	}

	public static class RecommendPage extends OneTableTemplatePage {

		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addTablePagerBean(pp);

			final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "RecommendPage_editPage",
					RecommendEditPage.class);
			addWindowBean(pp, "RecommendPage_edit", ajaxRequest).setHeight(300).setWidth(500)
					.setTitle($m("NewsMgrActions.0"));
		}

		protected TablePagerBean addTablePagerBean(final PageParameter pp) {
			final TablePagerBean tablePager = super.addTablePagerBean(pp, "RecommendationPage_tbl",
					RecommendationTbl.class);
			tablePager.addColumn(new TablePagerColumn("desc", $m("NewsMgrActions.1")))
					.addColumn(new TablePagerColumn("rlevel", $m("NewsMgrActions.2"), 50))
					.addColumn(TablePagerColumn.DATE("ddate", $m("NewsMgrActions.3")))
					.addColumn(new TablePagerColumn("status", $m("NewsMgrActions.4"), 70))
					.addColumn(TablePagerColumn.OPE(70));
			return tablePager;
		}

		@Override
		public ElementList getRightElements(final PageParameter pp) {
			final News news = NewsUtils.getNews(pp);
			return ElementList.of(LinkButton.addBtn().setOnclick(
					"$Actions['RecommendPage_edit']('newsId=" + news.getId() + "');"));
		}
	}

	public static class RecommendationTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final News news = NewsUtils.getNews(cp);
			cp.addFormParameter("newsId", news.getId());
			return _newsRecommendService.queryRecommends(news);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final NewsRecommend r = (NewsRecommend) dataObject;
			final KVMap row = new KVMap().add("desc", r.getDescription()).add("rlevel", r.getRlevel());
			return row;
		}
	}

	public static class RecommendEditPage extends FormTableRowTemplatePage {
		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addCalendarBean(pp, "RecommendEditPage_cal").setShowTime(true).setDateFormat(
					"yyyy-MM-dd HH:mm");
		}

		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			NewsRecommend r = _newsRecommendService.getBean(cp.getParameter("r_id"));
			final boolean insert = r == null;
			if (insert) {
				r = _newsRecommendService.createBean();
				final News news = NewsUtils.getNews(cp);
				r.setNewsId(news.getId());
			}
			r.setRlevel(cp.getIntParameter("r_rlevel"));
			r.setDstartDate(cp.getDateParameter("r_dstartdate"));
			r.setDendDate(cp.getDateParameter("r_denddate"));
			r.setDescription(cp.getParameter("r_description"));
			if (insert) {
				_newsRecommendService.insert(r);
			} else {
				_newsRecommendService.update(r);
			}

			final JavascriptForward js = super.onSave(cp);
			return js;
		}

		@Override
		public String getLabelWidth(final PageParameter pp) {
			return "70px";
		}

		@Override
		public ElementList getLeftElements(final PageParameter pp) {
			return ElementList.of(new SpanElement($m("NewsMgrActions.7")));
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final NewsRecommend r = _newsRecommendService.getBean(pp.getParameter("rid"));
			final InputElement r_id = InputElement.hidden("r_id");
			final ArrayList<Option> al = new ArrayList<Option>();
			for (int i = 1; i <= 5; i++) {
				final Option opt = new Option(i);
				if (r != null) {
					opt.setSelected(r.getRlevel() == i);
				}
				al.add(opt);
			}
			final InputElement r_rlevel = InputElement.select("r_rlevel").addElements(
					al.toArray(new Option[al.size()]));
			final CalendarInput r_dstartdate = new CalendarInput("r_dstartdate")
					.setCalendarComponent("RecommendEditPage_cal");
			final CalendarInput r_denddate = new CalendarInput("r_denddate")
					.setCalendarComponent("RecommendEditPage_cal");
			final InputElement r_description = InputElement.textarea("r_description").setRows(4);

			if (r != null) {
				r_id.setVal(r.getId());
				r_dstartdate.setVal(r.getDstartDate());
				r_denddate.setVal(r.getDendDate());
				r_description.setVal(r.getDescription());
			}

			final TableRow r1 = new TableRow(new RowField($m("NewsMgrActions.2"), InputElement.hidden(
					"newsId").setValue(pp), r_id, r_rlevel));
			final TableRow r2 = new TableRow(new RowField($m("NewsMgrActions.5"), r_dstartdate),
					new RowField($m("NewsMgrActions.6"), r_denddate));
			final TableRow r3 = new TableRow(new RowField($m("NewsMgrActions.1"), r_description));
			return TableRows.of(r1, r2, r3);
		}
	}
}