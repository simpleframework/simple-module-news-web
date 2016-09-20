package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;

import net.simpleframework.ado.ColumnData;
import net.simpleframework.ado.EFilterOpe;
import net.simpleframework.ado.EFilterRelation;
import net.simpleframework.ado.FilterItem;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ETimePeriod;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.module.common.content.AbstractContentBean.EContentStatus;
import net.simpleframework.module.common.web.content.ContentUtils;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.news.bean.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsPageletCreator;
import net.simpleframework.module.news.web.NewsUrlsFactory;
import net.simpleframework.module.news.web.page.t1.NewsFormBasePage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.TextForward;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.ETabMatch;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LabelElement;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.PhotoImage;
import net.simpleframework.mvc.common.element.SearchInput;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.template.lets.AdvSearchPage;
import net.simpleframework.mvc.template.lets.List_PageletsPage;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.EImageDot;
import net.simpleframework.mvc.template.struct.FilterButton;
import net.simpleframework.mvc.template.struct.FilterButtons;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.struct.Pagelet;
import net.simpleframework.mvc.template.struct.Pagelets;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsListTPage extends List_PageletsPage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(NewsListTPage.class, "/news_t2.css");

		addTablePagerBean(pp);

		addAjaxRequest(pp, "NewsListTPage_SearchPage", NewsAdvSearchPage.class);
		addWindowBean(pp, "NewsListTPage_SearchWindow").setContentRef("NewsListTPage_SearchPage")
				.setTitle($m("NewsListTPage.1")).setXdelta(-440).setYdelta(2).setWidth(500)
				.setHeight(300).setPopup(true);

		// PageletTab
		addPageletTabAjaxRequest(pp);
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		return addTablePagerBean(pp, "NewsListTPage_tbl", NewsList.class).setShowLineNo(false);
	}

	@Override
	protected TabButtons getCategoryTabs(final PageParameter pp) {
		final NewsUrlsFactory uFactory = ((INewsWebContext) newsContext).getUrlsFactory();
		final TabButtons btns = TabButtons.of();
		btns.add(new TabButton($m("NewsListTPage.4"),
				uFactory.getUrl(pp, NewsListTPage.class, (NewsCategory) null)));

		NewsCategory category = getNewsCategory(pp);
		final IDataQuery<?> dq = _newsCategoryService.queryChildren(
				category == null ? null : _newsCategoryService.getBean(category.getParentId()));
		int i = 0;
		while ((category = (NewsCategory) dq.next()) != null) {
			if (i++ > 3) {
				break;
			}
			btns.add(
					new TabButton(category.getText(), uFactory.getUrl(pp, NewsListTPage.class, category))
							.setTabMatch(ETabMatch.params));
		}
		addSearchTab(pp, btns);
		return btns;
	}

	protected void addSearchTab(final PageParameter pp, final TabButtons btns) {
		final String t = pp.getParameter("t");
		if (StringUtils.hasText(t)) {
			btns.append(new TabButton($m("NewsListTPage.5"), "#")).setSelectedIndex(btns.size() - 1);
		}
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final NewsUrlsFactory uFactory = ((INewsWebContext) newsContext).getUrlsFactory();
		final NewsCategory category = getNewsCategory(pp);
		return ElementList.of(new SearchInput("NewsListTPage_search")
				.setOnSearchClick("$Actions.loc('"
						+ HttpUtils.addParameters(
								uFactory.getUrl(pp, NewsListTPage.class, (NewsCategory) null), "t=")
						+ "' + encodeURIComponent($F('NewsListTPage_search')))")
				.setOnAdvClick("$Actions['NewsListTPage_SearchWindow']('" + AdvSearchPage
						.encodeRefererUrl(uFactory.getUrl(pp, NewsListTPage.class, category)) + "');")
				.setText(StringUtils.blank(pp.getLocaleParameter("t"))));
	}

	public IForward doPageletTab(final ComponentParameter cp) {
		final NewsPageletCreator creator = ((INewsWebContext) newsContext).getPageletCreator();

		final ETimePeriod tp = Convert.toEnum(ETimePeriod.class, cp.getParameter("time"));

		final IDataQuery<?> dq = _newsService.queryBeans(getNewsCategory(cp), new TimePeriod(tp),
				ColumnData.DESC(cp.getParameter("let")));

		return new TextForward(
				cp.wrapHTMLContextPath(creator.create(cp, dq).setDotIcon(EImageDot.numDot).toString()));
	}

	@Override
	protected Pagelets getPagelets(final PageParameter pp) {
		final NewsCategory category = getNewsCategory(pp);
		final Object categoryId = category == null ? "" : category.getId();
		final NewsPageletCreator creator = ((INewsWebContext) newsContext).getPageletCreator();

		final Pagelets lets = Pagelets.of();
		// 按评论
		IDataQuery<?> dq = _newsService.queryBeans(category, TimePeriod.week,
				ColumnData.DESC("comments"));
		lets.add(new Pagelet(new CategoryItem($m("NewsListTPage.2")),
				creator.create(pp, dq).setDotIcon(EImageDot.numDot))
						.setTabs(creator.createTimePeriodTabs("let=comments&categoryId=" + categoryId)));
		// 按浏览
		dq = _newsService.queryBeans(category, TimePeriod.week, ColumnData.DESC("views"));
		lets.add(new Pagelet(new CategoryItem($m("NewsListTPage.3")),
				creator.create(pp, dq).setDotIcon(EImageDot.numDot))
						.setTabs(creator.createTimePeriodTabs("let=views&categoryId=" + categoryId)));
		// 历史记录
		lets.add(creator.getHistoryPagelet(pp));
		return lets;
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		final NavigationButtons btns = NavigationButtons.of();
		NewsCategory category = _newsCategoryService.getBean(pp.getParameter("categoryId"));
		if (category == null) {
			btns.add(new SpanElement($m("NewsListTPage.4")));
		} else {
			final ArrayList<NewsCategory> al = new ArrayList<NewsCategory>();
			while (category != null) {
				al.add(category);
				category = _newsCategoryService.getBean(category.getParentId());
			}
			for (int i = al.size() - 1; i >= 0; i--) {
				category = al.get(i);
				final String txt = category.getText();
				AbstractElement<?> link;
				if (i > 0) {
					link = new LinkElement(txt).setHref(((INewsWebContext) newsContext).getUrlsFactory()
							.getUrl(pp, NewsListTPage.class, category));
				} else {
					link = new LabelElement(txt);
				}
				btns.add(link);
			}
		}
		return btns;
	}

	@Override
	public FilterButtons getFilterButtons(final PageParameter pp) {
		final String url = ((INewsWebContext) newsContext).getUrlsFactory().getUrl(pp,
				NewsListTPage.class, getNewsCategory(pp));
		final FilterButtons btns = FilterButtons.of();
		final NewsAdvSearchPage sPage = singleton(NewsAdvSearchPage.class);
		FilterButton btn = sPage.createFilterButton(pp, url, "as_topic");
		if (btn != null) {
			btns.add(btn.setLabel($m("NewsAdvSearchPage.0")));
		}
		btn = sPage.createFilterButton(pp, url, "as_author");
		if (btn != null) {
			btns.add(btn.setLabel($m("NewsAdvSearchPage.1")));
		}
		btn = sPage.createFilterDateButton(pp, url, "as_time");
		if (btn != null) {
			btns.add(btn.setLabel($m("AdvSearchPage.0")));
		}
		return btns;
	}

	public static NewsCategory getNewsCategory(final PageParameter pp) {
		return getCacheBean(pp, _newsCategoryService, "categoryId");
	}

	protected Checkbox createMyFilterCheckbox(final PageParameter pp) {
		final Checkbox cb = new Checkbox("NewsListTPage_myFilter", $m("NewsListTPage.6"))
				.setChecked("my".equals(pp.getParameter("f")));
		String referer = pp.getRefererParam();
		if (StringUtils.hasText(referer)) {
			if (cb.isChecked()) {
				referer = HttpUtils.addParameters(referer, new KVMap().add("f", null));
			} else {
				referer = HttpUtils.addParameters(referer, "f=my");
			}
			cb.setOnclick(JS.loc(referer));
		}
		return cb;
	}

	public static class NewsList extends ListTemplatePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final String t = cp.getLocaleParameter("t");
			if (StringUtils.hasText(t)) {
				return _newsService.getLuceneService().query(t, News.class);
			}

			final FilterItems params = FilterItems.of();
			if ("my".equals(cp.getParameter("f"))) {
				ID loginId;
				if ((loginId = cp.getLoginId()) != null) {
					params.add(new FilterItem("userId", loginId));
					params.add(
							new FilterItem("status", EFilterRelation.lt_equal, EContentStatus.publish));
				} else {
					params.add(FilterItem.FALSE);
				}
			} else {
				final FilterItem status = new FilterItem("status", EContentStatus.publish);
				params.add(status);
				ID loginId;
				if ((loginId = cp.getLoginId()) != null) {
					status.setLbracket(true);
					params.add(
							new FilterItem("userId", loginId).setRbracket(true).setOpe(EFilterOpe.or));
				}
			}

			// category
			final NewsCategory category = getNewsCategory(cp);
			if (category != null) {
				params.add(new FilterItem("categoryId", category.getId()));
				cp.addFormParameter("categoryId", category.getId());
			}

			// 条件过滤
			final String topic = cp.getLocaleParameter("as_topic");
			if (StringUtils.hasText(topic)) {
				params.addLike("topic", topic);
			}
			final String author = cp.getLocaleParameter("as_author");
			if (StringUtils.hasText(author)) {
				params.addLike("author", author);
			}
			params.addEqual("createDate", new TimePeriod(cp.getParameter("as_time")));
			return _newsService.queryByParams(params);
		}

		@Override
		protected Document doc(final ComponentParameter cp, final Object dataObject) {
			return ((News) dataObject).doc();
		}

		protected static final String LINE_SEP = "&nbsp;&nbsp;";

		@Override
		protected String toHTML_desc(final ComponentParameter cp, final Object dataObject) {
			final String t = cp.getParameter("t");
			if (StringUtils.hasText(t)) {
				return toHTML_desc(cp, dataObject, LINE_SEP, 80);
			}
			return super.toHTML_desc(cp, dataObject);
		}

		@Override
		protected String toHTML_topic(final ComponentParameter cp, final Object dataObject) {
			final News news = (News) dataObject;
			if (news.getUserId().equals(cp.getLoginId()) && news.getStatus() == EContentStatus.edit) {
				final StringBuilder sb = new StringBuilder();
				sb.append(SpanElement.colora00(getDataProperty(cp, dataObject, OP_TOPIC))
						.setClassName("f3 nt").setItalic(true));
				sb.append(SpanElement.SPACE).append("[ ")
						.append(new LinkElement($m("Edit")).setHref(getTopicEditUrl(cp, news)))
						.append(" ]");
				return sb.toString();
			}
			return super.toHTML_topic(cp, dataObject);
		}

		protected String getTopicEditUrl(final ComponentParameter cp, final News news) {
			return HttpUtils.addParameters(url(NewsFormBasePage.class), "newsId=" + news.getId());
		}

		@Override
		protected String toHTML_image(final ComponentParameter cp, final Object dataObject) {
			final String t = cp.getParameter("t");
			if (!StringUtils.hasText(t)) {
				return new PhotoImage(ContentUtils.getImagePath(cp, newsContext.getAttachmentService(),
						doc(cp, dataObject).select("img").first(), 128, 128)).toString();
			}
			return "";
		}

		@Override
		protected Object getDataProperty(final ComponentParameter cp, final Object dataObject,
				final String key) {
			if (OP_TOPIC_URL.equals(key)) {
				return ((INewsWebContext) newsContext).getUrlsFactory().getUrl(cp, NewsViewTPage.class,
						(News) dataObject);
			} else if (OP_DATE.equals(key)) {
				return ((News) dataObject).getCreateDate();
			}
			return super.getDataProperty(cp, dataObject, key);
		}
	}

	public static class NewsAdvSearchPage extends AdvSearchPage {
		@Override
		protected String[] getFilterParams() {
			return new String[] { "as_topic", "as_author", "as_time" };
		}

		@Override
		public String toItemsHTML(final PageParameter pp) {
			final StringBuilder sb = new StringBuilder();
			sb.append(addSearchItem(pp, $m("NewsAdvSearchPage.0"), new InputElement("as_topic")));
			sb.append(addSearchItem(pp, $m("NewsAdvSearchPage.1"), new InputElement("as_author")));
			sb.append(addSearchDateItem(pp, "as_time"));
			return sb.toString();
		}
	}
}
