package net.simpleframework.module.news.web;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.common.ID;
import net.simpleframework.module.favorite.IFavoriteContent;
import net.simpleframework.module.favorite.plugin.IFavoritePlugin;
import net.simpleframework.module.favorite.web.FavoriteWebRef;
import net.simpleframework.module.favorite.web.plugin.AbstractWebFavoritePlugin;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.news.bean.NewsCategory;
import net.simpleframework.module.news.web.page.NewsViewTPage;
import net.simpleframework.mvc.PageParameter;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsFavoriteRef extends FavoriteWebRef implements INewsContextAware {

	@Override
	protected Class<? extends IFavoritePlugin> getPluginClass() {
		return NewsWebFavoritePlugin.class;
	}

	public static class NewsWebFavoritePlugin extends AbstractWebFavoritePlugin {
		@Override
		public String getText() {
			return $m("NewsFavoriteRef.0");
		}

		@Override
		public IFavoriteContent getContent(final PageParameter pp, final Object contentId) {
			final News news = _newsService.getBean(contentId);
			return new AbstractFavoriteContent(news) {
				@Override
				public ID getCategoryId() {
					return news.getCategoryId();
				}

				@Override
				public String getUrl() {
					return ((INewsWebContext) newsContext).getUrlsFactory().getUrl(pp,
							NewsViewTPage.class, news);
				}
			};
		}

		@Override
		public String getCategoryText(final Object categoryId) {
			final NewsCategory category = _newsCategoryService.getBean(categoryId);
			return category != null ? category.getText() : null;
		}

		@Override
		public int getOrder() {
			return 11;
		}
	}
}
