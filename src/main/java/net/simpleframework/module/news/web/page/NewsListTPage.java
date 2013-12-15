package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;

import net.simpleframework.ado.ColumnData;
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
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.news.INewsCategoryService;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.INewsService;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsPageletCreator;
import net.simpleframework.module.news.web.NewsUrlsFactory;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.IMVCConst;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.TextForward;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.ETabMatch;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LabelElement;
import net.simpleframework.mvc.common.element.LinkElement;
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
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

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
		final NewsUrlsFactory uFactory = ((INewsWebContext) context).getUrlsFactory();
		final TabButtons btns = TabButtons.of();
		btns.add(new TabButton($m("NewsListTPage.4"), uFactory.getNewsListUrl(pp, null)));

		final INewsCategoryService service = context.getNewsCategoryService();
		NewsCategory category = getNewsCategory(pp);
		final IDataQuery<?> dq = service.queryChildren(category == null ? null : service
				.getBean(category.getParentId()));
		int i = 0;
		while ((category = (NewsCategory) dq.next()) != null) {
			if (i++ > 3) {
				break;
			}
			btns.add(new TabButton(category.getText(), uFactory.getNewsListUrl(pp, category))
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
		final NewsUrlsFactory uFactory = ((INewsWebContext) context).getUrlsFactory();
		final NewsCategory category = getNewsCategory(pp);
		return ElementList.of(new SearchInput("NewsListTPage_search")
				.setOnSearchClick(
						"$Actions.loc('"
								+ HttpUtils.addParameters(uFactory.getNewsListUrl(pp, null), "t=")
								+ "' + encodeURIComponent($F('NewsListTPage_search')))")
				.setOnAdvClick(
						"$Actions['NewsListTPage_SearchWindow']('"
								+ AdvSearchPage.encodeRefererUrl(uFactory.getNewsListUrl(pp, category))
								+ "');").setText(StringUtils.blank(pp.getLocaleParameter("t"))));
	}

	public IForward doPageletTab(final ComponentParameter cp) {
		final INewsService service = context.getNewsService();
		final NewsPageletCreator creator = ((INewsWebContext) context).getPageletCreator();

		final ETimePeriod tp = Convert.toEnum(ETimePeriod.class, cp.getParameter("time"));

		final IDataQuery<?> dq = service.queryBeans(getNewsCategory(cp), new TimePeriod(tp),
				ColumnData.DESC(cp.getParameter("let")));

		return new TextForward(cp.wrapHTMLContextPath(creator.create(cp, dq)
				.setDotIcon(EImageDot.numDot).toString()));
	}

	@Override
	protected Pagelets getPagelets(final PageParameter pp) {
		final INewsService service = context.getNewsService();
		final NewsCategory category = getNewsCategory(pp);
		final Object categoryId = category.getId();
		final NewsPageletCreator creator = ((INewsWebContext) context).getPageletCreator();

		final Pagelets lets = Pagelets.of();
		// 按评论
		IDataQuery<?> dq = service.queryBeans(category, TimePeriod.week, ColumnData.DESC("comments"));
		lets.add(new Pagelet(new CategoryItem($m("NewsListTPage.2")), creator.create(pp, dq)
				.setDotIcon(EImageDot.numDot)).setTabs(creator
				.createTimePeriodTabs("let=comments&categoryId=" + categoryId)));
		// 按浏览
		dq = service.queryBeans(category, TimePeriod.week, ColumnData.DESC("views"));
		lets.add(new Pagelet(new CategoryItem($m("NewsListTPage.3")), creator.create(pp, dq)
				.setDotIcon(EImageDot.numDot)).setTabs(creator
				.createTimePeriodTabs("let=views&categoryId=" + categoryId)));
		// 历史记录
		lets.add(creator.getHistoryPagelet(pp));
		return lets;
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		final NavigationButtons btns = NavigationButtons.of();
		final INewsCategoryService service = context.getNewsCategoryService();
		NewsCategory category = service.getBean(pp.getParameter("categoryId"));
		if (category == null) {
			btns.add(new SpanElement($m("NewsListTPage.4")));
		} else {
			final ArrayList<NewsCategory> al = new ArrayList<NewsCategory>();
			while (category != null) {
				al.add(category);
				category = service.getBean(category.getParentId());
			}
			for (int i = al.size() - 1; i >= 0; i--) {
				category = al.get(i);
				final String txt = category.getText();
				AbstractElement<?> link;
				if (i > 0) {
					link = new LinkElement(txt).setHref(((INewsWebContext) context).getUrlsFactory()
							.getNewsListUrl(pp, category));
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
		final String url = ((INewsWebContext) context).getUrlsFactory().getNewsListUrl(pp,
				getNewsCategory(pp));
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
		return getCacheBean(pp, context.getNewsCategoryService(), "categoryId");
	}

	protected Checkbox createMyFilterCheckbox(final PageParameter pp) {
		final Checkbox cb = new Checkbox("NewsListTPage_myFilter", $m("NewsListTPage.6"))
				.setChecked("my".equals(pp.getParameter("f")));
		String referer = pp.getParameter(IMVCConst.PARAM_REFERER);
		if (StringUtils.hasText(referer)) {
			if (cb.isChecked()) {
				referer = HttpUtils.addParameters(referer, new KVMap().add("f", null));
			} else {
				referer = HttpUtils.addParameters(referer, "f=my");
			}
			cb.setOnclick("$Actions.loc('" + referer + "')");
		}
		return cb;
	}

	public static class NewsList extends ListTemplatePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final String t = cp.getLocaleParameter("t");
			final INewsService nService = context.getNewsService();
			if (StringUtils.hasText(t)) {
				return nService.getLuceneService().query(t, News.class);
			}

			final FilterItems params = FilterItems.of();
			if ("my".equals(cp.getParameter("f"))) {
				ID loginId;
				if ((loginId = cp.getLoginId()) != null) {
					params.add(new FilterItem("userId", loginId));
					params.add(new FilterItem("status", EFilterRelation.lt_equal, EContentStatus.publish));
				} else {
					params.add(FilterItem.FALSE);
				}
			} else {
				final FilterItem status = new FilterItem("status", EContentStatus.publish);
				ID loginId;
				if ((loginId = cp.getLoginId()) != null) {
					status.setOrItem(new FilterItem("userId", loginId));
				}
				params.add(status);
			}

			// category
			final NewsCategory category = getNewsCategory(cp);
			if (category != null) {
				params.add(new FilterItem("categoryId", category.getId()));
				cp.addFormParameter("categoryId", category.getId());
			}

			// 条件过滤
			params.append(
					new FilterItem("topic", EFilterRelation.like, cp.getLocaleParameter("as_topic")))
					.append(
							new FilterItem("author", EFilterRelation.like, cp
									.getLocaleParameter("as_author")))
					.append(new FilterItem("createDate", new TimePeriod(cp.getParameter("as_time"))));
			return nService.queryByParams(params);
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
			return toHTML_desc(cp, dataObject, LINE_SEP, 240);
		}

		@Override
		protected String toHTML_topic(final ComponentParameter cp, final Object dataObject) {
			final News news = (News) dataObject;
			if (news.getUserId().equals(cp.getLoginId()) && news.getStatus() == EContentStatus.edit) {
				final StringBuilder sb = new StringBuilder();
				sb.append(new SpanElement(getDataProperty(cp, dataObject, OP_TOPIC))
						.setClassName("f3 nt").setColor("#b00").setItalic(true));
				sb.append(SpanElement.SPACE).append("[ ")
						.append(new LinkElement($m("Edit")).setHref(getTopicEditUrl(cp, news)))
						.append(" ]");
				return sb.toString();
			}
			return super.toHTML_topic(cp, dataObject);
		}

		protected String getTopicEditUrl(final ComponentParameter cp, final News news) {
			String url = ((INewsWebContext) context).getUrlsFactory().getNewsFormUrl(cp, news);
			final String referer = cp.getParameter(IMVCConst.PARAM_REFERER);
			if (StringUtils.hasText(referer)) {
				url = HttpUtils.addParameters(url, "url=" + HttpUtils.encodeUrl(referer));
			}
			return url;
		}

		@Override
		protected String toHTML_image(final ComponentParameter cp, final Object dataObject) {
			final String t = cp.getParameter("t");
			if (StringUtils.hasText(t)) {
				return "";
			}
			return super.toHTML_image(cp, dataObject);
		}

		@Override
		protected Object getDataProperty(final ComponentParameter cp, final Object dataObject,
				final String key) {
			if (OP_TOPIC_URL.equals(key)) {
				return ((INewsWebContext) context).getUrlsFactory().getNewsUrl(cp, (News) dataObject);
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
