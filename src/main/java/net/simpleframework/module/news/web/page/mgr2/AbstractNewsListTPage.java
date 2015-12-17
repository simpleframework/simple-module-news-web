package net.simpleframework.module.news.web.page.mgr2;

import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.lets.Category_ListPage;

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

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = addTablePagerBean(pp, "AbstractNewsListTPage_tbl",
				NewsListTbl.class, false).setFilter(true).setShowHead(true).setShowCheckbox(true);
		tablePager.addColumn(TablePagerColumn.ICON().setWidth(16))
				.addColumn(TablePagerColumn.OPE(80));
		return tablePager;
	}

	public static class NewsListTbl extends AbstractDbTablePagerHandler {
	}
}