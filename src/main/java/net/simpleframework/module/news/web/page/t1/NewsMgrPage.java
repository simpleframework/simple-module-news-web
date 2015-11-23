package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;
import java.util.Date;

import net.simpleframework.ado.FilterItem;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.web.content.page.AbstractRecommendationPage;
import net.simpleframework.module.common.web.page.AbstractDescPage;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.INewsService;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsLogRef.NewsUpdateLogPage;
import net.simpleframework.module.news.web.NewsUrlsFactory;
import net.simpleframework.module.news.web.page.NewsCategoryHandle;
import net.simpleframework.module.news.web.page.NewsFormTPage;
import net.simpleframework.module.news.web.page.NewsListTbl;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.module.news.web.page.NewsViewTPage;
import net.simpleframework.module.news.web.page.t2.NewsListPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.Icon;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LabelElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.NavigationTitle;
import net.simpleframework.mvc.component.ui.pager.db.NavigationTitle.NavigationTitleCallback;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/mgr")
public class NewsMgrPage extends CategoryTableLCTemplatePage implements INewsContextAware {

	public static EContentStatus[] STATUS_ARR = new EContentStatus[] { EContentStatus.edit,
			EContentStatus.publish, EContentStatus.lock };

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(NewsFormTPage.class, "/news.css");

		addCategoryBean(pp, NewsCategoryHandle.class);

		addTablePagerBean(pp, NewsListTbl.class)
				.addColumn(new TablePagerColumn("topic", $m("NewsMgrPage.1")))
				.addColumn(
						new TablePagerColumn("views", $m("NewsMgrPage.2"), 70)
								.setPropertyClass(Float.class))
				.addColumn(
						new TablePagerColumn("comments", $m("NewsMgrPage.3"), 70)
								.setTextAlign(ETextAlign.center))
				.addColumn(TablePagerColumn.DATE("createDate", $m("NewsMgrPage.4")))
				.addColumn(new TablePagerColumn("status", $m("NewsMgrPage.5"), 70) {
					@Override
					protected Option[] getFilterOptions() {
						return Option.from(STATUS_ARR);
					};
				}).addColumn(TablePagerColumn.OPE(120));

		// edit
		addAjaxRequest(pp, "NewsMgrPage_edit").setHandlerMethod("doEdit");

		// status
		addAjaxRequest(pp, "NewsMgrPage_status").setHandlerMethod("doStatus");

		// delete
		addAjaxRequest(pp, "NewsMgrPage_delete").setConfirmMessage($m("NewsMgrPage.11"))
				.setHandlerMethod("doDelete");

		// status
		addAjaxRequest(pp, "NewsMgrPage_statusPage", StatusDescPage.class);
		addWindowBean(pp, "NewsMgrPage_statusWindow").setContentRef("NewsMgrPage_statusPage")
				.setWidth(420).setHeight(240);

		// 推荐
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "NewsMgrPage_recommendationPage",
				RecommendationPage.class);
		addWindowBean(pp, "NewsMgrPage_recommendation", ajaxRequest).setHeight(240).setWidth(450)
				.setTitle($m("AbstractContentBean.2"));

		// log window
		final IModuleRef ref = ((INewsWebContext) newsContext).getLogRef();
		if (ref != null) {
			ajaxRequest = addAjaxRequest(pp, "NewsMgrPage_update_logPage", NewsUpdateLogPage.class);
			addWindowBean(pp, "NewsMgrPage_update_log", ajaxRequest).setHeight(540).setWidth(864);
		}

		// comment window
		ajaxRequest = addAjaxRequest(pp, "NewsMgrPage_commentPage", NewsCommentPage.class);
		addWindowBean(pp, "NewsMgrPage_commentWindow", ajaxRequest).setHeight(540).setWidth(864);

		// adv window
		ajaxRequest = addAjaxRequest(pp, "NewsMgrPage_advPage", NewsAdvPage.class);
		addWindowBean(pp, "NewsMgrPage_advWindow", ajaxRequest).setTitle($m("NewsMgrPage.13"))
				.setHeight(280).setWidth(420);
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return newsContext.getModule().getManagerRole();
	}

	public IForward doEdit(final ComponentParameter cp) {
		final JavascriptForward js = new JavascriptForward();
		final News news = _newsService.getBean(cp.getParameter("newsId"));
		final EContentStatus status = news.getStatus();
		if (status == EContentStatus.edit) {
			js.append(JS.loc(((INewsWebContext) newsContext).getUrlsFactory().getUrl(cp,
					NewsFormBasePage.class, news)));
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

	private LinkButton createStatusButton(final EContentStatus status) {
		return act_btn("NewsMgrPage_status", status.toString(), "newsId", "op=" + status.name());
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final LinkButton add = new LinkButton($m("NewsMgrPage.6"));
		final ElementList btns = ElementList.of(add).append(SpanElement.SPACE);
		final EContentStatus status = pp.getEnumParameter(EContentStatus.class, "status");
		if (status != EContentStatus.delete) {
			btns.append(createStatusButton(EContentStatus.publish))
					.append(createStatusButton(EContentStatus.lock)).append(SpanElement.SPACE);
		}
		btns.append(createStatusButton(EContentStatus.delete).setIconClass(Icon.trash))
				.append(SpanElement.SPACE)
				.add(createStatusButton(EContentStatus.edit).setText($m("NewsMgrPage.7")));

		if (pp.isLmember(newsContext.getModule().getManagerRole())) {
			btns.append(SpanElement.SPACE).add(
					new LinkButton($m("NewsMgrPage.13"))
							.setOnclick("$Actions['NewsMgrPage_advWindow']();"));
		}

		final NewsUrlsFactory uFactory = ((INewsWebContext) newsContext).getUrlsFactory();
		String url = uFactory.getUrl(pp, NewsFormBasePage.class, (News) null);
		final NewsCategory category = NewsUtils.getNewsCategory(pp);
		if (category != null) {
			url += "?categoryId=" + category.getId();
			btns.append(SpanElement.SPACE).append(
					new LinkButton($m("Button.Preview")).setOnclick(JS.loc(
							uFactory.getUrl(pp, NewsListPage.class, category), true)));
		}
		add.setOnclick(JS.loc(url));
		return btns;
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		return ElementList.of(NavigationTitle.toElement(pp, NewsUtils.getNewsCategory(pp),
				new NavigationTitleCallback<NewsCategory>() {
					@Override
					protected String getRootText() {
						return $m("NewsCategoryHandle.0");
					}

					@Override
					protected NewsCategory get(final Object id) {
						return _newsCategoryService.getBean(id);
					}

					@Override
					protected String getComponentTable() {
						return COMPONENT_TABLE;
					}

					@Override
					protected String getText(final NewsCategory t) {
						return t.toString() + SpanElement.shortText("(" + t.getName() + ")");
					}
				}));
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		return super.getNavigationBar(pp).append(new LabelElement($m("NewsMgrPage.0")));
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		final TabButton cTab = new TabButton($m("NewsCommentMgrPage.0"),
				url(NewsCommentMgrPage.class));
		final int c = _newsCommentService.queryByParams(
				FilterItems.of(new FilterItem("createdate", TimePeriod.day))).getCount();
		if (c > 0) {
			cTab.setStat(c);
		}
		final TabButtons tabs = TabButtons.of(new TabButton($m("NewsMgrPage.0"),
				url(NewsMgrPage.class)), cTab);
		return tabs;
	}

	public static class RecommendationPage extends AbstractRecommendationPage<News> {

		@Override
		protected INewsService getBeanService() {
			return _newsService;
		}

		@Override
		protected News getBean(final PageParameter pp) {
			return NewsViewTPage.getNews(pp);
		}

		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final JavascriptForward js = super.onSave(cp);
			js.append(createTableRefresh().toString());
			return js;
		}
	}

	public static class StatusDescPage extends AbstractDescPage implements INewsContextAware {

		@Override
		@Transaction(context = INewsContext.class)
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final EContentStatus op = cp.getEnumParameter(EContentStatus.class, "op");
			for (final String id : StringUtils.split(cp.getParameter("newsId"), ";")) {
				final News news = _newsService.getBean(id);
				setLogDescription(cp, news);
				news.setStatus(op);
				_newsService.update(new String[] { "status" }, news);
			}
			return super.onSave(cp)
					.append(CategoryTableLCTemplatePage.createTableRefresh().toString());
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
}
