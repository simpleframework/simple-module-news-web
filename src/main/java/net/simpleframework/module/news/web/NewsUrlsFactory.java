package net.simpleframework.module.news.web;

import net.simpleframework.common.StringUtils;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.page.t1.NewsFormAttachPage;
import net.simpleframework.module.news.web.page.t1.NewsFormBasePage;
import net.simpleframework.module.news.web.page.t1.NewsFormVotePage;
import net.simpleframework.module.news.web.page.t1.NewsMgrPage;
import net.simpleframework.module.news.web.page.t2.NewsListPage;
import net.simpleframework.module.news.web.page.t2.NewsViewPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.UrlsCache;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsUrlsFactory extends UrlsCache {

	public NewsUrlsFactory() {
		put(NewsMgrPage.class);

		put(NewsListPage.class);
		put(NewsViewPage.class);

		put(NewsFormBasePage.class);
		put(NewsFormAttachPage.class);
		put(NewsFormVotePage.class);
	}

	public String getUrl(final PageParameter pp, final Class<? extends AbstractMVCPage> mClass,
			final NewsCategory category) {
		return getUrl(pp, mClass, category, null);
	}

	public String getUrl(final PageParameter pp, final Class<? extends AbstractMVCPage> mClass,
			final NewsCategory category, final String params) {
		return getUrl(
				pp,
				mClass,
				StringUtils.join(new String[] {
						category != null ? "categoryId=" + category.getId() : null, params }, "&"));
	}

	public String getUrl(final PageParameter pp, final Class<? extends AbstractMVCPage> mClass,
			final News news) {
		return getUrl(pp, mClass, news, null);
	}

	public String getUrl(final PageParameter pp, final Class<? extends AbstractMVCPage> mClass,
			final News news, final String params) {
		return getUrl(pp, mClass, StringUtils.join(
				new String[] { news != null ? "newsId=" + news.getId() : null, params }, "&"));
	}
}
