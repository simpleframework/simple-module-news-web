package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ado.FilterItem;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.web.content.page.AbstractRecommendationPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.INewsService;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsLogRef.NewsUpdateLogPage;
import net.simpleframework.module.news.web.page.NewsCategoryHandle;
import net.simpleframework.module.news.web.page.NewsFormTPage;
import net.simpleframework.module.news.web.page.NewsListTbl;
import net.simpleframework.module.news.web.page.NewsMgrActions;
import net.simpleframework.module.news.web.page.NewsMgrActions.StatusDescPage;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.Icon;
import net.simpleframework.mvc.common.element.LabelElement;
import net.simpleframework.mvc.common.element.LinkButton;
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

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(NewsFormTPage.class, "/news.css");

		addCategoryBean(pp, NewsCategoryHandle.class);

		addTablePagerBean(pp, NewsListTbl.class)
				.addColumn(TablePagerColumn.ICON())
				.addColumn(new TablePagerColumn("topic", $m("NewsMgrPage.1")))
				.addColumn(
						new TablePagerColumn("views", $m("NewsMgrPage.2"), 70)
								.setPropertyClass(Float.class))
				.addColumn(
						new TablePagerColumn("comments", $m("NewsMgrPage.3"), 70)
								.setTextAlign(ETextAlign.center))
				.addColumn(TablePagerColumn.DATE("createDate", $m("NewsMgrPage.4")))
				.addColumn(TablePagerColumn.OPE(120));

		// edit
		addAjaxRequest(pp, "NewsMgrPage_edit").setHandlerMethod("doEdit").setHandlerClass(
				NewsMgrActions.class);
		// delete
		addAjaxRequest(pp, "NewsMgrPage_delete").setConfirmMessage($m("NewsMgrPage.11"))
				.setHandlerMethod("doDelete").setHandlerClass(NewsMgrActions.class);
		// status
		addAjaxRequest(pp, "NewsMgrPage_status").setHandlerMethod("doStatus").setHandlerClass(
				NewsMgrActions.class);

		// status window
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

	private LinkButton createStatusButton(final EContentStatus status) {
		return act_btn("NewsMgrPage_status", status.toString(), "newsId", "op=" + status.name());
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final ElementList btns = ElementList.of(NewsUtils.createAddNew(pp)).append(SpanElement.SPACE);
		final EContentStatus status = pp.getEnumParameter(EContentStatus.class, "status");
		if (status != EContentStatus.delete) {
			btns.append(createStatusButton(EContentStatus.publish))
					.append(createStatusButton(EContentStatus.lock)).append(SpanElement.SPACE);
		}
		btns.append(createStatusButton(EContentStatus.delete).setIconClass(Icon.trash))
				.append(SpanElement.SPACE)
				.append(createStatusButton(EContentStatus.edit).setText($m("NewsMgrPage.7")));

		if (pp.isLmember(newsContext.getModule().getManagerRole())) {
			btns.append(SpanElement.SPACE).append(
					new LinkButton($m("NewsMgrPage.13"))
							.setOnclick("$Actions['NewsMgrPage_advWindow']();"));
		}

		final LinkButton preview = NewsUtils.createNewsPreview(pp);
		if (preview != null) {
			btns.append(SpanElement.SPACE).append(preview);
		}
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
			return NewsUtils.getNews(pp);
		}

		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final JavascriptForward js = super.onSave(cp);
			js.append(createTableRefresh().toString());
			return js;
		}
	}
}
