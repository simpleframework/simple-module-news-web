package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsUrlsFactory;
import net.simpleframework.module.news.web.NewsVoteRef;
import net.simpleframework.module.news.web.page.NewsForm;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SupElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.template.t1.T1FormTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/form")
public class NewsFormBasePage extends T1FormTemplatePage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(NewsForm.class, "/news.css");
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return newsContext.getModule().getManagerRole();
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='NewsFormBasePage'>");
		sb.append(includeForm(pp));
		sb.append("</div>");
		return sb.toString();
	}

	protected String includeForm(final PageParameter pp) {
		return pp.includeUrl(NewsForm.class);
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final NewsUrlsFactory uFactory = ((INewsWebContext) newsContext).getUrlsFactory();
		final LinkButton backBtn = LinkButton.backBtn();
		final String url = pp.getParameter("url");
		if (StringUtils.hasText(url)) {
			backBtn.setHref(url);
		} else {
			backBtn.setOnclick("$Actions.loc('"
					+ uFactory.getUrl(pp, NewsMgrPage.class, (NewsCategory) null)
					+ "?categoryId=' + $F('ne_categoryId'));");
		}
		final ElementList el = ElementList.of(backBtn);
		if ("save".equals(pp.getParameter("op"))) {
			el.append(titleElement($m("NewsFormBasePage.3",
					Convert.toDateString(new Date(), "MM-dd HH:mm"))));
		}
		return el;
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		final NewsUrlsFactory uFactory = ((INewsWebContext) newsContext).getUrlsFactory();
		final News news = newsContext.getNewsService().getBean(pp.getParameter("newsId"));
		final TabButtons tabs = TabButtons.of(new TabButton($m("NewsFormBasePage.0"), uFactory
				.getUrl(pp, NewsFormBasePage.class, news)));
		if (news != null) {
			String t1 = $m("NewsFormBasePage.1");
			final int attachs = newsContext.getAttachmentService().queryByContent(news).getCount();
			if (attachs > 0) {
				t1 += SupElement.num(attachs);
			}
			tabs.append(new TabButton(t1, uFactory.getUrl(pp, NewsFormAttachPage.class, news)));
			final IModuleRef ref = ((INewsWebContext) newsContext).getVoteRef();
			if (ref != null) {
				String t2 = $m("NewsFormBasePage.2");
				final int votes = ((NewsVoteRef) ref).queryVotes(news).getCount();
				if (votes > 0) {
					t2 += SupElement.num(votes);
				}
				tabs.append(new TabButton(t2, uFactory.getUrl(pp, NewsFormVotePage.class, news)));
			}
		}
		return tabs;
	}
}
