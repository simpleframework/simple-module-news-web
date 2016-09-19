package net.simpleframework.module.news.web;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ctx.IContextBase;
import net.simpleframework.module.common.plugin.ModulePluginFactory;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.vote.VoteRef;
import net.simpleframework.module.vote.web.VoteListHandler;
import net.simpleframework.module.vote.web.page.VotePostPage;
import net.simpleframework.module.vote.web.plugin.AbstractWebVotePlugin;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsVoteRef extends VoteRef {

	@Override
	public void onInit(final IContextBase context) throws Exception {
		super.onInit(context);

		getModuleContext().getPluginRegistry().registPlugin(NewsWebVotePlugin.class);
	}

	public IDataQuery<?> queryVotes(final News news) {
		if (news == null) {
			return DataQueryUtils.nullQuery();
		}
		return getModuleContext().getVoteService().queryVotes(news.getId());
	}

	public TablePagerBean addVotesTbl(final PageParameter pp) {
		final TablePagerBean tablePager = getVotePlugin().addVoteComponent_Tbl(pp);
		final TablePagerColumn col = tablePager.getColumns().get("userId");
		if (col != null) {
			col.setVisible(false);
		}
		return tablePager;
	}

	public AbstractElement<?> toAddVoteElement(final PageParameter pp, final Object contentId) {
		return getVotePlugin().toAddVoteElement(pp, contentId);
	}

	public static NewsWebVotePlugin getVotePlugin() {
		return ModulePluginFactory.get(NewsWebVotePlugin.class);
	}

	public static class NewsWebVotePlugin extends AbstractWebVotePlugin {
		@Override
		public AbstractElement<?> toAddVoteElement(final PageParameter pp, final Object contentId) {
			final LinkButton lb = (LinkButton) super.toAddVoteElement(pp, contentId);
			if (contentId == null) {
				lb.setOnclick("alert('" + $m("NewsVoteRef.0") + "');");
			}
			return lb;
		}

		@Override
		public String getText() {
			return $m("NewsFavoriteRef.0");
		}
	}

	public static class _VoteListHandler extends VoteListHandler {
	}

	public static class _VotePostPage extends VotePostPage {
	}
}
