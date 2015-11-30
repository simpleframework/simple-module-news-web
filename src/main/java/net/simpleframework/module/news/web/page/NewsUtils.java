package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.ID;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsUrlsFactory;
import net.simpleframework.module.news.web.page.t1.NewsFormBasePage;
import net.simpleframework.module.news.web.page.t2.NewsListPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class NewsUtils implements INewsContextAware {

	public static NewsCategory getNewsCategory(final PageParameter pp) {
		return AbstractTemplatePage.getCacheBean(pp, _newsCategoryService, "categoryId");
	}

	public static ID getDomainId(final PageParameter pp) {
		return AbstractMVCPage.getPermissionOrg(pp).getId();
	}

	public static News getNews(final PageParameter pp) {
		return AbstractTemplatePage.getCacheBean(pp, _newsService, "newsId");
	}

	public static String getIconPath(final ComponentParameter cp, final NewsCategory category) {
		final String imgBase = cp.getCssResourceHomePath(NewsFormTPage.class) + "/images/";
		if (!cp.isLmanager() && category.getDomainId() == null) {
			return imgBase + "folder_lock.png";
		} else {
			return imgBase + "folder.png";
		}
	}

	static NewsUrlsFactory uFactory = ((INewsWebContext) newsContext).getUrlsFactory();

	public static LinkButton createAddNew(final PageParameter pp) {
		String url = uFactory.getUrl(pp, NewsFormBasePage.class, (News) null);
		final NewsCategory category = getNewsCategory(pp);
		if (category != null) {
			url += "?categoryId=" + category.getId();
		}
		return new LinkButton($m("NewsMgrPage.6")).setOnclick(JS.loc(url));
	}

	public static LinkButton createNewsPreview(final PageParameter pp) {
		final NewsCategory category = getNewsCategory(pp);
		if (category != null) {
			return new LinkButton($m("Button.Preview")).setOnclick(JS.loc(
					uFactory.getUrl(pp, NewsListPage.class, category), true));
		}
		return null;
	}
}
