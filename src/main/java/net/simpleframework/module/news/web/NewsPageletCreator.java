package net.simpleframework.module.news.web;

import static net.simpleframework.common.I18n.$m;

import java.util.Calendar;
import java.util.Date;

import net.simpleframework.ctx.service.ado.IADOBeanService;
import net.simpleframework.module.common.web.content.ListRowHandler;
import net.simpleframework.module.common.web.content.PageletCreator;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.template.struct.Pagelet;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsPageletCreator extends PageletCreator<News> implements INewsContextAware {

	public Pagelet getHistoryPagelet(final PageParameter pp) {
		return getHistoryPagelet(pp, "news_views");
	}

	@Override
	protected ListRowHandler<News> getDefaultListRowHandler() {
		return DEFAULT_HANDLER;
	}

	private final NewsListRowHandler DEFAULT_HANDLER = new NewsListRowHandler();

	public static class NewsListRowHandler extends ListRowHandler<News> {
		@Override
		protected String getHref(final PageParameter pp, final News news) {
			return ((INewsWebContext) context).getUrlsFactory().getNewsUrl(pp, news);
		}

		@Override
		protected String[] getShortDesc(final News news) {
			final int c = news.getComments();
			final long v = news.getViews();
			final StringBuilder sb = new StringBuilder();
			final Date lastCommentDate = news.getLastCommentDate();
			if (lastCommentDate != null) {
				final Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.HOUR_OF_DAY, -12);
				if (lastCommentDate.after(cal.getTime())) {
					sb.append(new SpanElement(c).setStyle("color: red;"));
				}
			}
			if (sb.length() == 0) {
				sb.append(c);
			}
			sb.append("/").append(v);
			return new String[] { sb.toString(), $m("NewsPageletCreator.0", c, v) };
		}

		@Override
		protected IADOBeanService<News> getBeanService() {
			return context.getNewsService();
		}
	}
}
