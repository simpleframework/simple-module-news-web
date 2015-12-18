package net.simpleframework.module.news.web.page.mgr2;

import static net.simpleframework.common.I18n.$m;

import java.util.List;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
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

	protected abstract List<NewsCategory> getNewsCategoryList(PageParameter pp);

	@Override
	protected CategoryItems getCategoryList(final PageParameter pp) {
		final CategoryItems items = CategoryItems.of();
		final List<NewsCategory> list = getNewsCategoryList(pp);
		if (list != null && list.size() > 0) {
			final NewsCategory _category = NewsUtils.getNewsCategory(pp);
			for (final NewsCategory category : list) {
				final CategoryItem item = new CategoryItem(category.getText());
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

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp,
				"AbstractNewsListTPage_tbl", NewsListTbl.class, false).setResize(false).setFilter(true)
				.setShowHead(true).setShowCheckbox(true).setPageItems(30)
				.setPagerBarLayout(EPagerBarLayout.bottom);
		tablePager
				.addColumn(TablePagerColumn.ICON().setWidth(16))
				.addColumn(new TablePagerColumn("topic", $m("NewsMgrPage.1")).setSort(false))
				.addColumn(
						new TablePagerColumn("stat", $m("NewsMgrPage.2") + "/" + $m("NewsMgrPage.3"), 90)
								.setTextAlign(ETextAlign.center).setFilterSort(false))
				.addColumn(TablePagerColumn.DATE("createDate", $m("NewsMgrPage.4")))
				.addColumn(TablePagerColumn.OPE(70));
		return tablePager;
	}

	public static class NewsListTbl extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			return super.createDataObjectQuery(cp);
		}
	}
}