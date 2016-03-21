package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.news.bean.NewsRecommend;
import net.simpleframework.module.news.bean.NewsRecommend.ERecommendStatus;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.CalendarInput;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class RecommendMgrPage extends OneTableTemplatePage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addTablePagerBean(pp);

		// 编辑
		final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "RecommendPage_editPage",
				RecommendEditPage.class);
		addWindowBean(pp, "RecommendPage_edit", ajaxRequest).setHeight(300).setWidth(500)
				.setTitle($m("RecommendMgrPage.0"));

		// 删除
		addDeleteAjaxRequest(pp, "RecommendMgrPage_del");

		// 放弃
		addAjaxRequest(pp, "RecommendMgrPage_abort").setHandlerMethod("doAbort").setConfirmMessage(
				$m("RecommendMgrPage.8"));
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = super
				.addTablePagerBean(pp, "RecommendationPage_tbl", RecommendTbl.class).setFilter(false)
				.setSort(false).setShowCheckbox(false);
		tablePager
				.addColumn(new TablePagerColumn("desc", $m("RecommendMgrPage.1")))
				.addColumn(new TablePagerColumn("rlevel", $m("RecommendMgrPage.2"), 50))
				.addColumn(TablePagerColumn.DATE("ddate", $m("RecommendMgrPage.3")).setWidth(120))
				.addColumn(
						new TablePagerColumn("status", $m("RecommendMgrPage.4"), 55)
								.setTextAlign(ETextAlign.center)).addColumn(TablePagerColumn.OPE(70));
		return tablePager;
	}

	@Transaction(context = INewsContext.class)
	public IForward doAbort(final ComponentParameter cp) {
		final NewsRecommend r = _newsRecommendService.getBean(cp.getParameter("rid"));
		_newsRecommendService.doAbort(r);
		return new JavascriptForward("$Actions['RecommendationPage_tbl']();");
	}

	@Transaction(context = INewsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("rid"));
		_newsRecommendService.delete(ids);
		return new JavascriptForward("$Actions['RecommendationPage_tbl']();");
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final News news = NewsUtils.getNews(pp);
		return ElementList.of(LinkButton.addBtn().setOnclick(
				"$Actions['RecommendPage_edit']('newsId=" + news.getId() + "');"));
	}

	public static class RecommendTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final News news = NewsUtils.getNews(cp);
			cp.addFormParameter("newsId", news.getId());
			return _newsRecommendService.queryRecommends(news);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final NewsRecommend r = (NewsRecommend) dataObject;
			final KVMap row = new KVMap().add("desc", toDescHTML(cp, r)).add("rlevel", r.getRlevel())
					.add("ddate", toDseDateHTML(cp, r)).add("status", toStatusHTML(cp, r))
					.add(TablePagerColumn.OPE, toOpeHTML(cp, r));
			return row;
		}

		protected String toDescHTML(final ComponentParameter cp, final NewsRecommend r) {
			return new LinkElement(r.getDescription()).setOnclick(
					"$Actions['RecommendPage_edit']('rid=" + r.getId() + "');").toString();
		}

		protected String toStatusHTML(final ComponentParameter cp, final NewsRecommend r) {
			final ERecommendStatus status = r.getStatus();
			String color = null;
			if (status == ERecommendStatus.running) {
				color = "green";
			} else if (status == ERecommendStatus.abort) {
				color = "red";
			} else if (status == ERecommendStatus.ready) {
				color = "#999";
			}
			return SpanElement.color(status, color).toString();
		}

		protected String toDseDateHTML(final ComponentParameter cp, final NewsRecommend r) {
			final StringBuilder sb = new StringBuilder();
			final Date dstartDate = r.getDstartDate();
			final Date dendDate = r.getDendDate();
			if (dstartDate != null) {
				sb.append(Convert.toDateString(dstartDate));
			} else {
				sb.append("-");
			}
			sb.append("<br>");
			if (dendDate != null) {
				sb.append(Convert.toDateString(dendDate));
			} else {
				sb.append("-");
			}
			return sb.toString();
		}

		protected String toOpeHTML(final ComponentParameter cp, final NewsRecommend r) {
			final StringBuilder sb = new StringBuilder();
			final ERecommendStatus status = r.getStatus();
			if (status == ERecommendStatus.ready || status == ERecommendStatus.running) {
				sb.append(new ButtonElement(ERecommendStatus.abort)
						.setOnclick("$Actions['RecommendMgrPage_abort']('rid=" + r.getId() + "');"));
			} else {
				sb.append(ButtonElement.deleteBtn().setOnclick(
						"$Actions['RecommendMgrPage_del']('rid=" + r.getId() + "');"));
			}
			return sb.toString();
		}
	}

	public static class RecommendEditPage extends FormTableRowTemplatePage {
		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addCalendarBean(pp, "RecommendEditPage_cal").setShowTime(true).setDateFormat(
					"yyyy-MM-dd HH:mm");

			addFormValidationBean(pp).addValidators(
					new Validator(EValidatorMethod.required, "#r_description"));
		}

		@Transaction(context = INewsContext.class)
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
			js.append("$Actions['RecommendationPage_tbl']();");
			return js;
		}

		@Override
		public String getLabelWidth(final PageParameter pp) {
			return "70px";
		}

		@Override
		public ElementList getLeftElements(final PageParameter pp) {
			return ElementList.of(new SpanElement($m("RecommendMgrPage.7")));
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final InputElement newsId = InputElement.hidden("newsId");
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
				newsId.setVal(r.getNewsId());
				r_id.setVal(r.getId());
				r_dstartdate.setVal(r.getDstartDate());
				r_denddate.setVal(r.getDendDate());
				r_description.setVal(r.getDescription());
				if (r.getStatus().ordinal() > ERecommendStatus.ready.ordinal()) {
					r_dstartdate.setReadonly(true);
					r_denddate.setReadonly(true);
				}
			} else {
				newsId.setValue(pp);
			}

			final TableRow r1 = new TableRow(new RowField($m("RecommendMgrPage.2"), newsId, r_id,
					r_rlevel));
			final TableRow r2 = new TableRow(new RowField($m("RecommendMgrPage.5"), r_dstartdate),
					new RowField($m("RecommendMgrPage.6"), r_denddate));
			final TableRow r3 = new TableRow(new RowField($m("RecommendMgrPage.1"), r_description));
			return TableRows.of(r1, r2, r3);
		}
	}
}