package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.ArrayList;

import net.simpleframework.ado.bean.IIdBeanAware;
import net.simpleframework.ado.lucene.ILuceneManager;
import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.BeanUtils;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.ArrayUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.common.web.content.ContentUtils;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.news.bean.NewsAttachment;
import net.simpleframework.module.news.bean.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.INewsWebContext.AttachmentDownloadHandler;
import net.simpleframework.module.news.web.NewsFavoriteRef;
import net.simpleframework.module.news.web.NewsPageletCreator;
import net.simpleframework.module.news.web.NewsPageletCreator.NewsListRowHandler;
import net.simpleframework.module.news.web.NewsVoteRef;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.TextForward;
import net.simpleframework.mvc.common.DownloadUtils;
import net.simpleframework.mvc.common.element.EElementEvent;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.tooltip.ETipElement;
import net.simpleframework.mvc.component.ui.tooltip.ETipPosition;
import net.simpleframework.mvc.component.ui.tooltip.TipBean;
import net.simpleframework.mvc.component.ui.tooltip.TipBean.HideOn;
import net.simpleframework.mvc.component.ui.tooltip.TipBean.Hook;
import net.simpleframework.mvc.component.ui.tooltip.TooltipBean;
import net.simpleframework.mvc.template.lets.View_PageletsPage;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.EImageDot;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.struct.Pagelet;
import net.simpleframework.mvc.template.struct.Pagelets;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsViewTPage extends View_PageletsPage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(NewsViewTPage.class, "/news_t2.css");

		addAjaxRequest_Download(pp);
		// tooltip
		addTooltip(pp);

		// PageletTab
		addPageletTabAjaxRequest(pp);

		final News news = NewsUtils.getNews(pp);
		if (news.isAllowComments()) {
			addCommentBean(pp, NewsCommentHandler.class).setRole(getPageManagerRole(pp));
		}

		// 更新views
		ContentUtils.updateViews(pp, news, _newsService);
		// 记录到cookies
		ContentUtils.addViewsCookie(pp, "news_views", news.getId());
	}

	protected AjaxRequestBean addAjaxRequest_Download(final PageParameter pp) {
		return addAjaxRequest(pp, "NewsViewTPage_download").setHandlerMethod("doDownload");
	}

	protected AjaxRequestBean addTooltipPage(final PageParameter pp) {
		return addAjaxRequest(pp, "NewsViewTPage_tipPage", NewsAttachmentTooltipPage.class)
				.setRole(PermissionConst.ROLE_ANONYMOUS);
	}

	protected TooltipBean addTooltip(final PageParameter pp) {
		addTooltipPage(pp);
		final TooltipBean tooltip = addComponentBean(pp, "NewsViewTPage_tip", TooltipBean.class);
		final StringBuilder js = new StringBuilder();
		js.append("var s = element.readAttribute('onclick');");
		js.append("if (s.startsWith('$Actions')) {");
		js.append("  s = s.substring(s.indexOf('(\\'') + 2, s.indexOf('\\')'));");
		js.append("  element.setAttribute('params', s);");
		js.append("}");
		tooltip.addTip(new TipBean(tooltip)
				.setSelector(".View_PageletsPage a[onclick*='NewsViewTPage_download']")
				.setContentRef("NewsViewTPage_tipPage").setCache(true).setTitle($m("NewsViewTPage.4"))
				.setStem(ETipPosition.leftTop)
				.setHook(new Hook(ETipPosition.rightTop, ETipPosition.topLeft))
				.setHideOn(new HideOn(ETipElement.closeButton, EElementEvent.click)).setWidth(400)
				.setJsTipCreate(js.toString()));
		return tooltip;
	}

	public IForward doDownload(final ComponentParameter cp) throws IOException {
		final IAttachmentService<NewsAttachment> service = newsContext.getAttachmentService();
		final NewsAttachment attachment = service.getBean(cp.getParameter("id"));
		final JavascriptForward js = new JavascriptForward();
		if (attachment != null) {
			final AttachmentFile af = service.createAttachmentFile(attachment);
			js.append(
					JS.loc(DownloadUtils.getDownloadHref(af, AttachmentDownloadHandler.class), true));
		} else {
			js.append("alert('").append($m("NewsViewTPage.5")).append("');");
		}
		return js;
	}

	@Override
	public String getTopic2(final PageParameter pp) {
		final StringBuilder sb = new StringBuilder();
		final News news = NewsUtils.getNews(pp);
		final String author = news.getAuthor();
		if (StringUtils.hasText(author)) {
			sb.append($m("NewsViewTPage.0")).append(": ");
			sb.append(author).append(SpanElement.SEP());
		}
		final String source = news.getSource();
		if (StringUtils.hasText(source)) {
			sb.append($m("NewsViewTPage.1")).append(": ");
			sb.append(source).append(SpanElement.SEP());
		}
		final String keyWords = news.getKeyWords();
		if (StringUtils.hasText(keyWords)) {
			sb.append($m("NewsViewTPage.2")).append(": ");
			sb.append(keyWords).append(SpanElement.SEP());
		}
		sb.append(super.getTopic2(pp));

		// 收藏
		final IModuleRef ref = ((INewsWebContext) newsContext).getFavoriteRef();
		if (ref != null) {
			sb.append(SpanElement.SEP());
			sb.append(((NewsFavoriteRef) ref).toFavoriteElement(pp, news.getId()));
		}
		return sb.toString();
	}

	@Override
	protected Object getDataProperty(final PageParameter pp, final String key) {
		final News news = NewsUtils.getNews(pp);
		if (OP_DATE.equals(key)) {
			return news.getCreateDate();
		} else if (OP_CONTENT.equals(key)) {
			final StringBuilder sb = new StringBuilder();
			sb.append(ContentUtils.getContent(pp, newsContext.getAttachmentService(), news));
			// vote
			final IModuleRef ref = ((INewsWebContext) newsContext).getVoteRef();
			if (ref != null) {
				final IDataQuery<?> dq = ((NewsVoteRef) ref).queryVotes(news);
				if (dq.getCount() > 0) {
					sb.append("<div class='news_vote'>");
					IIdBeanAware bean;
					while ((bean = (IIdBeanAware) dq.next()) != null) {
						sb.append(
								pp.includeUrl(NewsVoteRef._VotePostPage.class, "voteId=" + bean.getId()));
					}
					sb.append("</div>");
				}
			}
			return sb.toString();
		}
		return BeanUtils.getProperty(news, key);
	}

	public IForward doPageletTab(final ComponentParameter cp) {
		final NewsPageletCreator creator = ((INewsWebContext) newsContext).getPageletCreator();

		// final ETimePeriod tp = Convert.toEnum(ETimePeriod.class,
		// cp.getParameter("time"));

		final IDataQuery<?> dq = DataQueryUtils.nullQuery();
		// _newsService.queryRecommendationBeans(
		// _newsCategoryService.getBean(cp.getParameter("categoryId")), new
		// TimePeriod(tp));
		return new TextForward(
				cp.wrapHTMLContextPath(creator.create(cp, dq).setDotIcon(EImageDot.numDot).toString()));
	}

	@Override
	protected Pagelets getPagelets(final PageParameter pp) {
		final News news = NewsUtils.getNews(pp);
		final NewsPageletCreator creator = ((INewsWebContext) newsContext).getPageletCreator();

		final Pagelets lets = Pagelets.of();

		// 按相关度
		final ILuceneManager lService = _newsService.getLuceneService();
		String[] arr = StringUtils.split(news.getKeyWords(), " ");
		if (arr.length < 3) {
			arr = ArrayUtils.add(arr, lService.getQueryTokens(news.getTopic()));
		}
		lets.add(new Pagelet(new CategoryItem($m("NewsViewTPage.3")), creator.create(pp,
				lService.query(null, StringUtils.join(arr, " "), News.class), new NewsListRowHandler() {
					@Override
					protected News toBean(final Object o) {
						final News news2 = super.toBean(o);
						return news2 != null && !news2.equals(news) ? news2 : null;
					}
				}).setDotIcon(EImageDot.imgDot1)));

		// 按推荐
		final ID categoryId = news.getCategoryId();
		final IDataQuery<?> dq = DataQueryUtils.nullQuery();
		// _newsService.queryRecommendationBeans(
		// _newsCategoryService.getBean(categoryId), TimePeriod.week);
		lets.add(new Pagelet(new CategoryItem($m("NewsViewTPage.6")),
				creator.create(pp, dq).setDotIcon(EImageDot.numDot))
						.setTabs(creator.createTimePeriodTabs("categoryId=" + categoryId)));

		// 历史记录
		lets.add(creator.getHistoryPagelet(pp));
		return lets;
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		final NavigationButtons btns = NavigationButtons.of();
		final News news = NewsUtils.getNews(pp);
		final ArrayList<NewsCategory> al = new ArrayList<>();
		NewsCategory category = _newsCategoryService.getBean(news.getCategoryId());
		while (category != null) {
			al.add(category);
			category = _newsCategoryService.getBean(category.getParentId());
		}
		for (int i = al.size() - 1; i >= 0; i--) {
			category = al.get(i);
			btns.add(new LinkElement(category.getText()).setHref(((INewsWebContext) newsContext)
					.getUrlsFactory().getUrl(pp, NewsListTPage.class, category)));
		}
		return btns;
	}
}