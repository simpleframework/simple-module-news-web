package net.simpleframework.module.news.web.page.t1;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.web.page.NewsForm;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.t1.T1ResizedTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/comment/mgr")
public class NewsCommentMgrPage extends T1ResizedTemplatePage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		pp.addImportCSS(NewsForm.class, "/news.css");

		addTablePagerBean(pp, "NewsCommentMgrPage_tbl").setPageItems(30)
				.setPagerBarLayout(EPagerBarLayout.bottom).setContainerId("tbl_" + hashId)
				.setHandleClass(CommentTbl.class);
	}

	@Override
	public String getRole(final PageParameter pp) {
		return context.getManagerRole();
	}

	@Override
	protected TabButtons getTabButtons(final PageParameter pp) {
		return singleton(NewsMgrPage.class).getTabButtons(pp);
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div align='center' class='NewsCommentMgrPage'>");
		sb.append("  <div id='tbl_").append(hashId).append("'></div>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class CommentTbl extends AbstractDbTablePagerHandler {
	}
}
