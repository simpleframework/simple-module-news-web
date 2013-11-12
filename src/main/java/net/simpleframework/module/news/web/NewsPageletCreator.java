package net.simpleframework.module.news.web;

import static net.simpleframework.common.I18n.$m;

import java.util.Arrays;

import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.service.ado.IADOBeanService;
import net.simpleframework.module.common.web.content.ListRowHandler;
import net.simpleframework.module.common.web.content.PageletCreator;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.EImageDot;
import net.simpleframework.mvc.template.struct.Pagelet;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class NewsPageletCreator extends PageletCreator<News> implements INewsContextAware {

	public Pagelet getHistoryPagelet(final PageParameter pp) {
		final String[] arr = StringUtils.split(pp.getCookie("news_lastviews"), "|");
		return new Pagelet(new CategoryItem($m("NewsPageletCreator.0")), create(
				arr == null ? null : Arrays.asList(arr), DEFAULT_HANDLER).setDotIcon(EImageDot.dot2));
	}

	@Override
	protected ListRowHandler<News> getDefaultListRowHandler() {
		return DEFAULT_HANDLER;
	}

	private final NewsListRowHandler DEFAULT_HANDLER = new NewsListRowHandler();

	public static class NewsListRowHandler extends ListRowHandler<News> {
		@Override
		protected String getHref(final News news) {
			return ((INewsWebContext) context).getUrlsFactory().getNewsUrl(news);
		}

		@Override
		protected String[] getShortDesc(final News news) {
			final int c = news.getComments();
			final long v = news.getViews();
			return new String[] { c + "/" + v, $m("NewsPageletCreator.1", c, v) };
		}

		@Override
		protected IADOBeanService<News> getBeanService() {
			return context.getNewsService();
		}
	}
}
