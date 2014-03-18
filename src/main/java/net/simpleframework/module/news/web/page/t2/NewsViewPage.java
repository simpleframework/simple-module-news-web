package net.simpleframework.module.news.web.page.t2;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.web.page.NewsViewTPage;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.t2.T2TemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/view")
public class NewsViewPage extends T2TemplatePage implements INewsContextAware {

	@Override
	protected boolean isPage404(final PageParameter pp) {
		return NewsViewTPage._isPage404(pp);
	}

	@Override
	public String getTitle(final PageParameter pp) {
		final News news = NewsViewTPage.getNews(pp);
		return news != null ? news.getTopic() : null;
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		return pp.includeUrl(NewsViewTPage.class);
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		final NavigationButtons btns = super.getNavigationBar(pp);
		btns.addAll(singleton(NewsViewTPage.class).getNavigationBar(pp));
		return btns;
	}
}
