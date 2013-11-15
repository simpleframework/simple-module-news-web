package net.simpleframework.module.news.web;

import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.page.t1.NewsFormAttachPage;
import net.simpleframework.module.news.web.page.t1.NewsFormBasePage;
import net.simpleframework.module.news.web.page.t1.NewsFormVotePage;
import net.simpleframework.module.news.web.page.t1.NewsMgrPage;
import net.simpleframework.module.news.web.page.t2.NewsListPage;
import net.simpleframework.module.news.web.page.t2.NewsViewPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.common.UrlsCache;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsUrlsFactory extends UrlsCache {

	protected Class<? extends AbstractMVCPage> getNewsListPage(final NewsCategory category) {
		return NewsListPage.class;
	}

	public String getNewsListUrl(final NewsCategory category) {
		String url = AbstractMVCPage.url(getNewsListPage(category));
		if (category != null) {
			url += "?categoryId=" + category.getId();
		}
		return url;
	}

	public String getNewsManagerUrl(final NewsCategory category) {
		String url = AbstractMVCPage.url(NewsMgrPage.class);
		if (category != null) {
			url += "?categoryId=" + category.getId();
		}
		return url;
	}

	protected Class<? extends AbstractMVCPage> getNewsFormPage(final News news) {
		return NewsFormBasePage.class;
	}

	public String getNewsFormUrl(final News news) {
		String url = AbstractMVCPage.url(getNewsFormPage(news));
		if (news != null) {
			url += "?newsId=" + news.getId();
		}
		return url;
	}

	public String getNewsForm_AttachUrl(final News news) {
		return AbstractMVCPage.url(NewsFormAttachPage.class, "newsId=" + news.getId());
	}

	public String getNewsForm_VoteUrl(final News news) {
		return AbstractMVCPage.url(NewsFormVotePage.class, "newsId=" + news.getId());
	}

	public String getNewsUrl(final News news) {
		return getNewsUrl(news, false);
	}

	protected Class<? extends AbstractMVCPage> getNewsViewPage(final News news) {
		return NewsViewPage.class;
	}

	public String getNewsUrl(final News news, final boolean preview) {
		String url = AbstractMVCPage.url(getNewsViewPage(news)) + "?newsId=" + news.getId();
		if (preview) {
			url += "&preview=true";
		}
		return url;
	}
}
