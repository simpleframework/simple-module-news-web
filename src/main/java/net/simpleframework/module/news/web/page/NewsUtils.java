package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.object.ObjectEx.CacheV;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.news.bean.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsUrlsFactory;
import net.simpleframework.module.news.web.page.t1.NewsFormBasePage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.db.NavigationTitle.NavigationTitleCallback;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class NewsUtils implements INewsContextAware {

	public static NewsCategory getNewsCategory(final PageParameter pp) {
		return pp.getRequestCache("_news_category", new CacheV<NewsCategory>() {
			@Override
			public NewsCategory get() {
				NewsCategory bean = _newsCategoryService.getBean(pp.getParameter("categoryId"));
				if (bean == null) {
					final String category = pp.getParameter("category");
					if (StringUtils.hasText(category)) {
						bean = _newsCategoryService.getBeanByName(category);
					}
				}
				return bean;
			}
		});
	}

	public static News getNews(final PageParameter pp) {
		return AbstractTemplatePage.getCacheBean(pp, _newsService, "newsId");
	}

	public static ID getDomainId(final PageParameter pp) {
		return AbstractMVCPage.getPermissionOrg(pp).getId();
	}

	public static String getIconPath(final ComponentParameter cp, final NewsCategory category) {
		final String imgBase = cp.getCssResourceHomePath(NewsFormTPage.class) + "/images/";
		if (!cp.isLmanager() && category.getDomainId() == null) {
			return imgBase + "folder_lock.png";
		} else {
			return imgBase + "folder.png";
		}
	}

	public static NavigationTitleCallback<NewsCategory> createNavigationTitleCallback(
			final PageParameter pp, final String tblname) {
		final EContentStatus status = pp.getEnumParameter(EContentStatus.class, "status");
		return new NavigationTitleCallback<NewsCategory>(
				status == EContentStatus.delete ? $m("NewsCategoryHandle.1")
						: $m("NewsCategoryHandle.0"), tblname) {
			@Override
			protected NewsCategory get(final Object id) {
				return _newsCategoryService.getBean(id);
			}

			@Override
			protected String getText(final NewsCategory t) {
				return t.toString() + SpanElement.shortText("(" + t.getName() + ")");
			}
		};
	}

	static NewsUrlsFactory uFactory = ((INewsWebContext) newsContext).getUrlsFactory();

	public static LinkButton createAddNew(final PageParameter pp) {
		return new LinkButton($m("NewsMgrPage.6")).setOnclick("$Actions.loc('"
				+ uFactory.getUrl(pp, NewsFormBasePage.class, (News) null, "categoryId=")
				+ "' + $F('.parameters #categoryId'));");
	}

	public static ButtonElement createStatusAct(final PageParameter pp, final EContentStatus status,
			final News news) {
		return new ButtonElement(status).setHighlight(status == EContentStatus.publish).setOnclick(
				"$Actions['NewsMgrPage_status']('op=" + status.name() + "&newsId=" + news.getId()
						+ "');");
	}

	public static LinkButton createNewsPreview(final PageParameter pp) {
		final NewsCategory category = getNewsCategory(pp);
		return category == null ? null : new LinkButton($m("Button.Preview")).setOnclick(JS.loc(
				uFactory.getUrl(pp, NewsListTPage.class, category), true));
	}
}
