package net.simpleframework.module.news.web.page.mgr2;

import java.util.Collection;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.module.common.content.AbstractContentBean.EContentStatus;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.news.bean.NewsCategory;
import net.simpleframework.module.news.bean.NewsStat;
import net.simpleframework.module.news.web.page.NewsListTbl;
import net.simpleframework.module.news.web.page.NewsMgrActions;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.module.news.web.page.mgr2.NewsMgrTPage.NewsListMgr2Tbl;
import net.simpleframework.module.news.web.page.mgr2.NewsMgrTPage._NewsMgrActions;
import net.simpleframework.module.news.web.page.mgr2.NewsMgrTPage._StatusDescPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.SessionCache;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.Icon;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.SupElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.template.lets.Category_ListPage;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.CategoryItems;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractNewsListTPage extends Category_ListPage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addTablePagerBean(pp);

		if (isPageManagerRole(pp)) {
			// edit/delete/status
			NewsMgrActions.addMgrComponentBean(pp, _NewsMgrActions.class, _StatusDescPage.class);
		}
	}

	protected Collection<NewsCategory> getNewsCategoryList(final PageParameter pp) {
		return null;
	}

	@Override
	protected CategoryItems getCategoryList(final PageParameter pp) {
		final CategoryItems items = CategoryItems.of();
		final Collection<NewsCategory> list = getNewsCategoryList(pp);
		if (list != null && list.size() > 0) {
			SessionCache.lput("_CATEGORY_LIST", list);
			final NewsCategory _category = NewsUtils.getNewsCategory(pp);
			for (final NewsCategory category : list) {
				final NewsStat stat = _newsStatService.getNewsStat(category.getId(), pp.getLDomainId());
				final CategoryItem item = new CategoryItem(category.getText());
				int nums;
				if (isPageManagerRole(pp)) {
					nums = stat.getNums() - stat.getNums_delete();
				} else {
					nums = stat.getNums_publish();
				}
				if (nums > 0) {
					item.setNum(new SupElement(nums));
				}
				item.setHref(HttpUtils.addParameters(pp.getParameter("_referer"),
						"category=" + category.getName()));
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

	protected LinkButton createAddNew(final PageParameter pp) {
		return NewsUtils.createAddNew(pp, null);
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		if (isPageManagerRole(pp)) {
			return ElementList.of(createAddNew(pp)).append(SpanElement.SPACE)
					.append(NewsMgrTPage.createStatusButton(EContentStatus.publish))
					.append(SpanElement.SPACE).append(NewsMgrTPage
							.createStatusButton(EContentStatus.delete).setIconClass(Icon.trash));
		}
		return super.getRightElements(pp);
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final boolean mgr = isPageManagerRole(pp);
		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp, "NewsMgrTPage_tbl",
				mgr ? NewsListMgr2Tbl2.class : NewsListTbl2.class, false).setFilter(true)
						.setShowLineNo(false).setShowHead(true).setShowCheckbox(mgr).setResize(false)
						.setPageItems(30).setPagerBarLayout(EPagerBarLayout.bottom);
		final TablePagerColumn TC_CREATEDATE = NewsListTbl.TC_CREATEDATE();
		tablePager.addColumn(TablePagerColumn.ICON()).addColumn(NewsListTbl.TC_TOPIC());
		if (mgr) {
			tablePager.addColumn(NewsListTbl.TC_STATUS());
		} else {
			tablePager.addColumn(NewsListTbl.TC_VIEWS());
			TC_CREATEDATE.setWidth(125);
		}
		tablePager.addColumn(NewsListTbl.TC_COMMENTS()).addColumn(TC_CREATEDATE);
		if (mgr) {
			tablePager.addColumn(TablePagerColumn.OPE(70));
		}
		return tablePager;
	}

	public static class NewsListMgr2Tbl2 extends NewsListMgr2Tbl {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final NewsCategory category = NewsUtils.getNewsCategory(cp);
			if (category == null) {
				return DataQueryUtils.nullQuery();
			}
			return super.createDataObjectQuery(cp);
		}
	}

	public static class NewsListTbl2 extends NewsListTbl {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final NewsCategory category = NewsUtils.getNewsCategory(cp);
			if (category == null) {
				return DataQueryUtils.nullQuery();
			}
			// 仅显示已发布的
			cp.putParameter("status", EContentStatus.publish.name());
			return super.createDataObjectQuery(cp);
		}

		@Override
		protected LinkElement createCategoryElement(final NewsCategory category) {
			return null;
		}

		@Override
		protected ImageElement createImageMark(final ComponentParameter cp, final News news) {
			return null;
		}

		@Override
		protected String toCommentsHTML(final ComponentParameter cp, final News news) {
			return String.valueOf(news.getComments());
		}
	}
}