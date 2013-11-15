package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsVoteRef;
import net.simpleframework.module.news.web.NewsVoteRef._VoteListHandler;
import net.simpleframework.module.news.web.page.NewsViewTPage;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/form/vote")
public class NewsFormVotePage extends NewsFormBasePage {

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='NewsFormVotePage'>").append(pp.includeUrl(NewsForm_Vote.class))
				.append("</div>");
		return sb.toString();
	}

	public static class NewsForm_Vote extends OneTableTemplatePage implements INewsContextAware {

		@Override
		protected void onForward(final PageParameter pp) {
			super.onForward(pp);

			final IModuleRef ref = ((INewsWebContext) context).getVoteRef();
			if (ref != null) {
				((NewsVoteRef) ref).addVotesTbl(pp).setShowFilterBar(false)
						.setNoResultDesc($m("NewsForm_Vote.0")).setPagerBarLayout(EPagerBarLayout.bottom)
						.setContainerId("table_" + hashId).setHandleClass(NewsVoteListHandler.class);
			}
		}

		@Override
		public ElementList getLeftElements(final PageParameter pp) {
			final IModuleRef ref = ((INewsWebContext) context).getVoteRef();
			if (ref != null) {
				final News news = NewsViewTPage.getNews(pp);
				return ElementList.of(((NewsVoteRef) ref).toAddVoteElement(pp,
						news != null ? news.getId() : null));
			}
			return null;
		}
	}

	public static class NewsVoteListHandler extends _VoteListHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final IModuleRef ref = ((INewsWebContext) context).getVoteRef();
			if (ref == null) {
				return DataQueryUtils.nullQuery();
			}
			final News news = NewsViewTPage.getNews(cp);
			if (news != null) {
				cp.addFormParameter("newsId", news.getId());
			}
			return ((NewsVoteRef) ref).queryVotes(news);
		}
	}
}
