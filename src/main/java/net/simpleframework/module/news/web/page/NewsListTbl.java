package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.page.t2.NewsViewPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumns;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.mvc.template.t1.ext.LCTemplateTablePagerHandler;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsListTbl extends LCTemplateTablePagerHandler implements INewsContextAware {
	@Override
	public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
		final NewsCategory category = NewsUtils.getNewsCategory(cp);
		if (category != null) {
			cp.addFormParameter("categoryId", category.getId());
		}
		final EContentStatus status = cp.getEnumParameter(EContentStatus.class, "status");
		if (status != null) {
			cp.addFormParameter("status", status.name());
		}
		return _newsService.queryBeans(category, status);
	}

	@Override
	public AbstractTablePagerSchema createTablePagerSchema() {
		return new DefaultTablePagerSchema() {

			@Override
			public TablePagerColumns getTablePagerColumns(final ComponentParameter cp) {
				final TablePagerColumns columns = new TablePagerColumns(super.getTablePagerColumns(cp));
				final EContentStatus status = cp.getEnumParameter(EContentStatus.class, "status");
				if (status == EContentStatus.delete) {
					columns.remove(4);
					columns
							.add(4, new TablePagerColumn("categoryId", $m("NewsMgrPage.10"), 150)
									.setFilter(false));
				}

				return columns;
			}

			@Override
			public Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
				final News news = (News) dataObject;
				final Map<String, Object> kv = new KVMap();
				final ID id = news.getId();
				final StringBuilder sb = new StringBuilder();
				final EContentStatus status = cp.getEnumParameter(EContentStatus.class, "status");
				if (status != EContentStatus.delete) {
					String className = "news_flag";
					if (news.isImageMark()) {
						className += " news_flag_image";
					}

					sb.append(new SpanElement().setClassName(className));
					final LinkElement le = new LinkElement(news.getTopic())
							.setOnclick("$Actions['NewsMgrPage_edit']('newsId=" + id + "');");
					if (news.getRecommendation() > 0) {
						le.addClassName("news_recommendation");
					}
					sb.append(le);
					kv.put("topic", sb.toString());
				} else {
					kv.put("topic", news.getTopic());
					final NewsCategory category = _newsCategoryService.getBean(news.getCategoryId());
					if (category != null) {
						kv.put("categoryId", category.getText());
					}
				}

				final EContentStatus status2 = news.getStatus();
				kv.put("status", new SpanElement().setClassName("news_status_" + status2.name())
						+ status2.toString());

				kv.put("views", news.getViews());
				if (status != EContentStatus.delete) {
					kv.put("comments", new ButtonElement(news.getComments())
							.setOnclick("$Actions['NewsMgrPage_commentWindow']('newsId=" + id + "');"));
				} else {
					kv.put("comments", news.getComments());
				}
				kv.put("createDate", news.getCreateDate());

				sb.setLength(0);
				if (status2 == EContentStatus.edit) {
					sb.append(new ButtonElement(EContentStatus.publish).setHighlight(true).setOnclick(
							"$Actions['NewsMgrPage_status']('op=publish&newsId=" + id + "');"));
				} else {
					sb.append(new ButtonElement($m("Button.Preview")).setOnclick(JS.loc(
							AbstractMVCPage.url(ViewControlPage.class, "newsId=" + id), true)));
				}
				sb.append(SpanElement.SPACE);
				sb.append(ButtonElement.logBtn()
						.setDisabled(((INewsWebContext) newsContext).getLogRef() == null)
						.setOnclick("$Actions['NewsMgrPage_update_log']('newsId=" + id + "');"));
				sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
				kv.put(TablePagerColumn.OPE, sb.toString());

				return kv;
			}

			@Override
			public Map<String, Object> getRowAttributes(final ComponentParameter cp,
					final Object dataObject) {
				final News news = (News) dataObject;
				final Map<String, Object> kv = super.getRowAttributes(cp, dataObject);
				final StringBuilder sb = new StringBuilder();
				final EContentStatus s = news.getStatus();
				if (s == EContentStatus.edit) {
					// 菜单索引
					sb.append(";5");
				}
				if (s == EContentStatus.publish) {
					sb.append(";3");
				}
				if (s == EContentStatus.lock) {
					sb.append(";4");
				}
				if (((INewsWebContext) newsContext).getLogRef() == null) {
					sb.append(";7");
				}
				if (sb.length() > 0) {
					kv.put(MENU_DISABLED, sb.substring(1));
				}
				return kv;
			}
		};
	}

	private MenuItem createMenuItem(final EContentStatus status) {
		return MenuItem.of(status.toString()).setOnclick_act("NewsMgrPage_status", "newsId",
				"op=" + status.name());
	}

	@Override
	public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
			final MenuItem menuItem) {
		if (menuItem == null) {
			final MenuItems items = MenuItems.of();
			final EContentStatus status = cp.getEnumParameter(EContentStatus.class, "status");
			if (status != EContentStatus.delete) {
				items.append(MenuItem.itemEdit().setOnclick_act("NewsMgrPage_edit", "newsId"))
						.append(MenuItem.sep())
						.append(
								MenuItem.of($m("Button.Preview"), null,
										"$Actions.loc('" + AbstractMVCPage.url(ViewControlPage.class)
												+ "?newsId=' + $pager_action(item).rowId(), true);"))
						.append(MenuItem.sep())
						.append(
								MenuItem.of($m("AbstractContentBean.2")).setOnclick_act(
										"NewsMgrPage_recommendation", "newsId")).append(MenuItem.sep())
						.append(createMenuItem(EContentStatus.publish))
						.append(createMenuItem(EContentStatus.lock)).append(MenuItem.sep());
			}
			items.append(createMenuItem(EContentStatus.edit).setTitle($m("NewsMgrPage.7")))
					.append(MenuItem.sep())
					.append(createMenuItem(EContentStatus.delete).setIconClass(MenuItem.ICON_DELETE))
					.append(MenuItem.sep())
					.append(MenuItem.itemLog().setOnclick_act("NewsMgrPage_update_log", "newsId"));
			return items;
		}
		return null;
	}

	@Override
	public Object getRowBeanById(final ComponentParameter cp, final Object id) {
		return _newsService.getBean(id);
	}

	public static class ViewControlPage extends AbstractTemplatePage {

		@Override
		protected String getRedirectUrl(final PageParameter pp) {
			final News news = NewsViewTPage.getNews(pp);
			return news == null ? PAGE404.getUrl() : ((INewsWebContext) newsContext).getUrlsFactory()
					.getUrl(pp, NewsViewPage.class, news, "preview=true");
		}
	}
}
