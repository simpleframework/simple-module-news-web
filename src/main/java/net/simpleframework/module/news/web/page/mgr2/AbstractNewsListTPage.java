package net.simpleframework.module.news.web.page.mgr2;

import static net.simpleframework.common.I18n.$m;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.page.NewsFormTPage;
import net.simpleframework.module.news.web.page.NewsListTbl;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.module.news.web.page.t1.NewsFormBasePage;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.SessionCache;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryTreeHandler;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.mvc.template.lets.Category_ListPage;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.CategoryItems;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractNewsListTPage extends Category_ListPage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addTablePagerBean(pp);
	}

	protected abstract Collection<NewsCategory> getNewsCategoryList(PageParameter pp);

	@Override
	protected CategoryItems getCategoryList(final PageParameter pp) {
		final CategoryItems items = CategoryItems.of();
		final Collection<NewsCategory> list = getNewsCategoryList(pp);
		if (list != null && list.size() > 0) {
			SessionCache.lput("_CATEGORY_LIST", list);
			final NewsCategory _category = NewsUtils.getNewsCategory(pp);
			for (final NewsCategory category : list) {
				final CategoryItem item = new CategoryItem(category.getText());
				doInitCategoryItem(pp, item, category);
				if (_category != null && _category.getId().equals(category.getId())) {
					item.setSelected(true);
				}
				items.add(item);
			}
			if (_category == null) {
				items.get(0).setSelected(true);
			}
		}
		return items;
	}

	protected abstract void doInitCategoryItem(PageParameter pp, CategoryItem item,
			NewsCategory category);

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		if (isPageManagerRole(pp)) {
			final ElementList btns = ElementList.of(
					NewsUtils.createAddNew(pp, _NewsFormBasePage.class), SpanElement.SPACE);
			return btns;
		}
		return super.getRightElements(pp);
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final boolean mgr = isPageManagerRole(pp);
		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp,
				"AbstractNewsListTPage_tbl", NewsViewListTbl.class, false).setFilter(true)
				.setShowLineNo(false).setShowHead(true).setShowCheckbox(mgr).setResize(false)
				.setPageItems(30).setPagerBarLayout(EPagerBarLayout.bottom);
		if (mgr) {
			tablePager.addColumn(TablePagerColumn.ICON());
		}
		tablePager
				.addColumn(new TablePagerColumn("topic", $m("NewsMgrPage.1")).setSort(false))
				.addColumn(
						new TablePagerColumn("stat", $m("NewsMgrPage.2") + "/" + $m("NewsMgrPage.3"), 90)
								.setTextAlign(ETextAlign.center).setFilterSort(false))
				.addColumn(TablePagerColumn.DATE("createDate", $m("NewsMgrPage.4")));
		if (mgr) {
			tablePager.addColumn(TablePagerColumn.OPE(70));
		}
		return tablePager;
	}

	public static class NewsViewListTbl extends NewsListTbl {
		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final News news = (News) dataObject;
			final KVMap kv = new KVMap();
			kv.add("topic", toTopicHTML(cp, news))
					.add("stat", news.getViews() + "/" + news.getComments())
					.add("createDate", news.getCreateDate());
			return kv;
		}
	}

	@PageMapping(url = "/news/form2")
	public static class _NewsFormBasePage extends NewsFormBasePage {
		@Override
		protected String includeForm(final PageParameter pp) {
			return pp.includeUrl(_NewsFormTPage.class);
		}
	}

	public static class _NewsFormTPage extends NewsFormTPage {
		@Override
		protected Class<? extends DictionaryTreeHandler> getCategoryDictClass(final PageParameter pp) {
			return _CategorySelectedTree.class;
		}
	}

	public static class _CategorySelectedTree extends DictionaryTreeHandler {
		@SuppressWarnings("unchecked")
		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			if (parent == null) {
				final List<NewsCategory> list = (List<NewsCategory>) SessionCache
						.lget("_CATEGORY_LIST");
				if (list != null) {
					final TreeNodes nodes = TreeNodes.of();
					for (final NewsCategory category : list) {
						final TreeNode tn = new TreeNode((TreeBean) cp.componentBean, parent, category);
						nodes.add(tn);
					}
					return nodes;
				}
			}
			return super.getTreenodes(cp, parent);
		}
	}
}