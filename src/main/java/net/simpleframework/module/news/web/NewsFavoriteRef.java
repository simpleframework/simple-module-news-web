package net.simpleframework.module.news.web;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.common.ID;
import net.simpleframework.ctx.IModuleContext;
import net.simpleframework.module.common.plugin.ModulePluginFactory;
import net.simpleframework.module.favorite.FavoriteRef;
import net.simpleframework.module.favorite.IFavoriteContent;
import net.simpleframework.module.favorite.web.plugin.AbstractWebFavoritePlugin;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class NewsFavoriteRef extends FavoriteRef implements INewsContextAware {

	@Override
	public void onInit(final IModuleContext context) throws Exception {
		super.onInit(context);

		getModuleContext().getPluginRegistry().registPlugin(NewsWebFavoritePlugin.class);
	}

	public AbstractElement<?> toFavoriteElement(final PageParameter pp, final Object contentId) {
		return plugin().toFavoriteOpElement(pp, contentId);
	}

	public NewsWebFavoritePlugin plugin() {
		return ModulePluginFactory.get(NewsWebFavoritePlugin.class);
	}

	public static class NewsWebFavoritePlugin extends AbstractWebFavoritePlugin {
		@Override
		public String getText() {
			return $m("NewsFavoriteRef.0");
		}

		@Override
		public IFavoriteContent getContent(final Object contentId) {
			final News news = context.getNewsService().getBean(contentId);
			return new AbstractFavoriteContent(news) {
				@Override
				public ID getCategoryId() {
					return news.getCategoryId();
				}

				@Override
				public String getUrl() {
					return ((INewsWebContext) context).getUrlsFactory().getNewsUrl(news);
				}
			};
		}

		@Override
		public String getCategoryText(final Object categoryId) {
			final NewsCategory category = context.getNewsCategoryService().getBean(categoryId);
			return category != null ? category.getText() : null;
		}

		@Override
		public int getOrder() {
			return 11;
		}
	}
}
