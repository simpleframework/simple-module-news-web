package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsVoteRef;
import net.simpleframework.module.news.web.page.NewsFormTPage;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.SessionCache;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SupElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.template.t1.T1FormTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/form")
public class NewsFormBasePage extends T1FormTemplatePage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(NewsFormTPage.class, "/news.css");
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return getPageManagerRole(pp);
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
		return pp.includeUrl(NewsFormTPage.class);
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final ElementList el = ElementList.of(createBackBtn(pp));
		final News news = NewsUtils.getNews(pp);
		if (news != null) {
			el.add(titleElement($m("NewsFormBasePage.3") + " : " + news.getStatus()));
		}
		return el;
	}

	protected LinkButton createBackBtn(final PageParameter pp) {
		final LinkButton backBtn = LinkButton.backBtn();
		String referer = pp.getRequestHeader("Referer");
		if (StringUtils.hasText(referer) && referer.contains("/news/")
				&& !referer.contains("/news/form")) {
			backBtn.setHref(referer);
			SessionCache.lput("_Referer", referer);
		} else {
			referer = (String) SessionCache.lget("_Referer");
			if (referer != null) {
				backBtn.setHref(referer);
			}
		}
		return backBtn;
	}

	protected Class<? extends NewsFormBasePage> getFormBasePageClass() {
		return NewsFormBasePage.class;
	}

	protected Class<? extends NewsFormAttachPage> getFormAttachPageClass() {
		return NewsFormAttachPage.class;
	}

	protected Class<? extends NewsFormVotePage> getFormVotePageClass() {
		return NewsFormVotePage.class;
	}

	protected String getTabUrl(final Class<? extends AbstractMVCPage> pageClass, final News news) {
		if (news == null) {
			return url(pageClass);
		} else {
			return HttpUtils.addParameters(url(pageClass), "newsId=" + news.getId());
		}
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		final News news = NewsUtils.getNews(pp);
		final TabButtons tabs = TabButtons.of(new TabButton($m("NewsFormBasePage.0"), getTabUrl(
				getFormBasePageClass(), news)));
		if (news != null) {
			final TabButton attach = getTabButton_attach(pp, news);
			if (attach != null) {
				tabs.append(attach);
			}
			final TabButton vote = getTabButton_vote(pp, news);
			if (vote != null) {
				tabs.append(vote);
			}
		}
		return tabs;
	}

	protected TabButton getTabButton_attach(final PageParameter pp, final News news) {
		String t1 = $m("NewsFormBasePage.1");
		final int attachs = newsContext.getAttachmentService().queryByContent(news).getCount();
		if (attachs > 0) {
			t1 += SupElement.num(attachs);
		}
		return new TabButton(t1, getTabUrl(getFormAttachPageClass(), news));
	}

	protected TabButton getTabButton_vote(final PageParameter pp, final News news) {
		final IModuleRef ref = ((INewsWebContext) newsContext).getVoteRef();
		if (ref != null) {
			String t2 = $m("NewsFormBasePage.2");
			final int votes = ((NewsVoteRef) ref).queryVotes(news).getCount();
			if (votes > 0) {
				t2 += SupElement.num(votes);
			}
			return new TabButton(t2, getTabUrl(getFormVotePageClass(), news));
		}
		return null;
	}
}
