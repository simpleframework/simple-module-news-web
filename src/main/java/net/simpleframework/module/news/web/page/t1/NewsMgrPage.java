package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ado.FilterItem;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.web.content.page.AbstractRecommendationPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.INewsService;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.web.page.NewsCategoryHandle;
import net.simpleframework.module.news.web.page.NewsFormTPage;
import net.simpleframework.module.news.web.page.NewsListTbl;
import net.simpleframework.module.news.web.page.NewsMgrActions;
import net.simpleframework.module.news.web.page.NewsMgrActions.StatusDescPage;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
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

		addTablePagerBean(pp, NewsListTbl.class).addColumn(TablePagerColumn.ICON())
				.addColumn(NewsListTbl.TC_TOPIC()).addColumn(NewsListTbl.TC_VIEWS())
				.addColumn(NewsListTbl.TC_COMMENTS()).addColumn(NewsListTbl.TC_CREATEDATE())
				.addColumn(TablePagerColumn.OPE(120));

		// edit/delete/status
		NewsMgrActions.addMgrComponentBean(pp, NewsMgrActions.class, StatusDescPage.class);

		// 推荐
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "NewsMgrPage_recommendationPage",
				RecommendationPage.class);
		addWindowBean(pp, "NewsMgrPage_recommendation", ajaxRequest).setHeight(240).setWidth(450)
				.setTitle($m("AbstractContentBean.2"));

		// adv window
		ajaxRequest = addAjaxRequest(pp, "NewsMgrPage_advPage", NewsAdvPage.class);
		addWindowBean(pp, "NewsMgrPage_advWindow", ajaxRequest).setTitle($m("NewsMgrPage.13"))
				.setHeight(280).setWidth(420);
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return newsContext.getModule().getManagerRole();
	}

	LinkButton createStatusButton(final EContentStatus status) {
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
				NewsUtils.createNavigationTitleCallback(pp, COMPONENT_TABLE)));
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
