package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.FilterItem;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.web.content.page.AbstractRecommendationPage;
import net.simpleframework.module.common.web.page.AbstractDescPage;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.INewsService;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsLogRef.NewsUpdateLogPage;
import net.simpleframework.module.news.web.NewsUrlsFactory;
import net.simpleframework.module.news.web.page.NewsForm;
import net.simpleframework.module.news.web.page.NewsViewTPage;
import net.simpleframework.module.news.web.page.t2.NewsListPage;
import net.simpleframework.module.news.web.page.t2.NewsViewPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.Icon;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LabelElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumns;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;
import net.simpleframework.mvc.template.t1.ext.LCTemplateTablePagerHandler;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/mgr")
public class NewsMgrPage extends CategoryTableLCTemplatePage implements INewsContextAware {

	public static EContentStatus[] STATUS_ARR = new EContentStatus[] { EContentStatus.edit,
			EContentStatus.publish, EContentStatus.lock };

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		pp.addImportCSS(NewsForm.class, "/news.css");

		addCategoryBean(pp, NewsCategoryHandle.class);

		addTablePagerBean(pp, NewsTableHandler.class)
				.addColumn(new TablePagerColumn("topic", $m("NewsMgrPage.1")))
				.addColumn(
						new TablePagerColumn("views", $m("NewsMgrPage.2"), 70)
								.setPropertyClass(Float.class))
				.addColumn(new TablePagerColumn("comments", $m("NewsMgrPage.3"), 70))
				.addColumn(
						new TablePagerColumn("createDate", $m("NewsMgrPage.4"), 120)
								.setPropertyClass(Date.class))
				.addColumn(new TablePagerColumn("status", $m("NewsMgrPage.5"), 70) {
					@Override
					protected Option[] getFilterOptions() {
						return Option.from(STATUS_ARR);
					};
				}).addColumn(TablePagerColumn.OPE().setWidth(140));

		// edit
		addAjaxRequest(pp, "NewsMgrPage_edit").setHandlerMethod("doEdit");

		// status
		addAjaxRequest(pp, "NewsMgrPage_status").setHandlerMethod("doStatus");

		// delete
		addAjaxRequest(pp, "NewsMgrPage_delete").setConfirmMessage($m("NewsMgrPage.11"))
				.setHandlerMethod("doDelete");

		// status
		addAjaxRequest(pp, "NewsMgrPage_statusPage", StatusDescPage.class);
		addWindowBean(pp, "NewsMgrPage_statusWindow").setContentRef("NewsMgrPage_statusPage")
				.setWidth(420).setHeight(240);

		// 推荐
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "NewsMgrPage_recommendationPage",
				RecommendationPage.class);
		addWindowBean(pp, "NewsMgrPage_recommendation", ajaxRequest).setHeight(240).setWidth(450)
				.setTitle($m("AbstractContentBean.2"));

		// log window
		final IModuleRef ref = ((INewsWebContext) newsContext).getLogRef();
		if (ref != null) {
			ajaxRequest = addAjaxRequest(pp, "NewsMgrPage_update_logPage", NewsUpdateLogPage.class);
			addWindowBean(pp, "NewsMgrPage_update_log", ajaxRequest).setHeight(540).setWidth(864);
		}

		// comment window
		ajaxRequest = addAjaxRequest(pp, "NewsMgrPage_commentPage", NewsCommentPage.class);
		addWindowBean(pp, "NewsMgrPage_commentWindow", ajaxRequest).setHeight(540).setWidth(864);

		// adv window
		ajaxRequest = addAjaxRequest(pp, "NewsMgrPage_advPage", NewsAdvPage.class);
		addWindowBean(pp, "NewsMgrPage_advWindow", ajaxRequest).setTitle($m("NewsMgrPage.13"))
				.setHeight(280).setWidth(420);
	}

	@Override
	public String getRole(final PageParameter pp) {
		return newsContext.getModule().getManagerRole();
	}

	public IForward doEdit(final ComponentParameter cp) {
		final JavascriptForward js = new JavascriptForward();
		final News news = newsContext.getNewsService().getBean(cp.getParameter("newsId"));
		final EContentStatus status = news.getStatus();
		if (status == EContentStatus.edit) {
			js.append("$Actions.loc('")
					.append(
							((INewsWebContext) newsContext).getUrlsFactory().getUrl(cp,
									NewsFormBasePage.class, news)).append("');");
		} else {
			js.append("if (confirm('").append($m("NewsMgrPage.8", status))
					.append("')) { $Actions['NewsMgrPage_statusWindow']('op=")
					.append(EContentStatus.edit.name()).append("&newsId=").append(news.getId())
					.append("'); }");
		}
		return js;
	}

	@Transaction(context = INewsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("newsId"));
		newsContext.getNewsService().delete(ids);
		return createTableRefresh();
	}

	public IForward doStatus(final ComponentParameter cp) {
		final JavascriptForward js = new JavascriptForward();
		final EContentStatus op = cp.getEnumParameter(EContentStatus.class, "op");
		final String newsId = cp.getParameter("newsId");
		final ArrayList<String> deletes = new ArrayList<String>();
		for (final String id : StringUtils.split(newsId, ";")) {
			final News news = newsContext.getNewsService().getBean(id);
			final EContentStatus status = news.getStatus();
			if (op == status && op != EContentStatus.delete) {
				js.append("alert('").append($m("NewsMgrPage.9", op)).append("');");
				return js;
			}
			if (status == EContentStatus.delete) {
				deletes.add(news.getId().toString());
			}
		}
		if (op == EContentStatus.delete && deletes.size() > 0) {
			js.append("$Actions['NewsMgrPage_delete']('newsId=")
					.append(StringUtils.join(deletes, ";")).append("')");
		} else {
			js.append("$Actions['NewsMgrPage_statusWindow']('op=").append(op.name())
					.append("&newsId=").append(newsId).append("');");
		}
		return js;
	}

	private LinkButton createStatusButton(final EContentStatus status) {
		return act_btn("NewsMgrPage_status", status.toString(), "newsId", "op=" + status.name());
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final LinkButton add = new LinkButton($m("NewsMgrPage.6"));
		final ElementList btns = ElementList.of(add).append(SpanElement.SPACE);
		final EContentStatus status = pp.getEnumParameter(EContentStatus.class, "status");
		if (status != EContentStatus.delete) {
			btns.append(createStatusButton(EContentStatus.publish))
					.append(createStatusButton(EContentStatus.lock)).append(SpanElement.SPACE);
		}
		btns.append(createStatusButton(EContentStatus.delete).setIconClass(Icon.trash))
				.append(SpanElement.SPACE)
				.add(createStatusButton(EContentStatus.edit).setText($m("NewsMgrPage.7")));

		if (pp.isLmember(newsContext.getModule().getManagerRole())) {
			btns.append(SpanElement.SPACE).add(
					new LinkButton($m("NewsMgrPage.13"))
							.setOnclick("$Actions['NewsMgrPage_advWindow']();"));
		}

		final NewsUrlsFactory uFactory = ((INewsWebContext) newsContext).getUrlsFactory();
		String url = uFactory.getUrl(pp, NewsFormBasePage.class, (News) null);
		final NewsCategory category = getNewsCategory(pp);
		if (category != null) {
			url += "?categoryId=" + category.getId();
			btns.append(SpanElement.SPACE).append(
					new LinkButton($m("Button.Preview")).setOnclick("$Actions.loc('"
							+ uFactory.getUrl(pp, NewsListPage.class, category) + "', true);"));
		}
		add.setOnclick("$Actions.loc('" + url + "');");
		return btns;
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		return super.getNavigationBar(pp).append(new LabelElement($m("NewsMgrPage.0")));
	}

	@Override
	public TabButtons getTabButtons(final PageParameter pp) {
		final TabButton cTab = new TabButton($m("NewsCommentMgrPage.0"),
				url(NewsCommentMgrPage.class));
		final int c = newsContext.getCommentService()
				.queryByParams(FilterItems.of(new FilterItem("createdate", TimePeriod.day))).getCount();
		if (c > 0) {
			cTab.setStat(c);
		}
		final TabButtons tabs = TabButtons.of(new TabButton($m("NewsMgrPage.0"),
				url(NewsMgrPage.class)), cTab);
		return tabs;
	}

	private static NewsCategory getNewsCategory(final PageParameter pp) {
		return getCacheBean(pp, newsContext.getNewsCategoryService(), "categoryId");
	}

	public static class NewsTableHandler extends LCTemplateTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final NewsCategory category = getNewsCategory(cp);
			if (category != null) {
				cp.addFormParameter("categoryId", category.getId());
			}
			final EContentStatus status = cp.getEnumParameter(EContentStatus.class, "status");
			if (status != null) {
				cp.addFormParameter("status", status.name());
			}
			return newsContext.getNewsService().queryBeans(category, status);
		}

		@Override
		public AbstractTablePagerSchema createTablePagerSchema() {
			return new DefaultTablePagerSchema() {

				@Override
				public TablePagerColumns getTablePagerColumns(final ComponentParameter cp) {
					final TablePagerColumns columns = new TablePagerColumns(
							super.getTablePagerColumns(cp));
					final EContentStatus status = cp.getEnumParameter(EContentStatus.class, "status");
					if (status == EContentStatus.delete) {
						columns.remove(4);
						columns.add(4, new TablePagerColumn("categoryId", $m("NewsMgrPage.10"), 150)
								.setFilter(false));
					}

					return columns;
				}

				@Override
				public Map<String, Object> getRowData(final ComponentParameter cp,
						final Object dataObject) {
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
						final NewsCategory category = newsContext.getNewsCategoryService().getBean(
								news.getCategoryId());
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
						sb.append(new ButtonElement(EContentStatus.publish).setHighlight(true)
								.setOnclick(
										"$Actions['NewsMgrPage_status']('op=publish&newsId=" + id + "');"));
					} else {
						sb.append(new ButtonElement($m("Button.Preview")).setOnclick("$Actions.loc('"
								+ url(ViewControlPage.class, "newsId=" + id) + "', true);"));
					}
					sb.append(SpanElement.SPACE);
					sb.append(ButtonElement.logBtn()
							.setDisabled(((INewsWebContext) newsContext).getLogRef() == null)
							.setOnclick("$Actions['NewsMgrPage_update_log']('newsId=" + id + "');"));
					sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
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
									MenuItem.of($m("Button.Preview"), null, "$Actions.loc('"
											+ url(ViewControlPage.class)
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
		protected ElementList getNavigationTitle(final ComponentParameter cp) {
			return doNavigationTitle(cp, getNewsCategory(cp),
					new NavigationTitleCallback<NewsCategory>() {
						@Override
						protected String rootText() {
							return $m("NewsCategoryHandle.0");
						}

						@Override
						protected NewsCategory get(final Object id) {
							return newsContext.getNewsCategoryService().getBean(id);
						}

						@Override
						protected String getText(final NewsCategory t) {
							return t.toString() + SpanElement.shortText("(" + t.getName() + ")");
						}
					});
		}

		@Override
		public Object getRowBeanById(final ComponentParameter cp, final Object id) {
			return newsContext.getNewsService().getBean(id);
		}
	}

	public static class ViewControlPage extends AbstractTemplatePage {

		@Override
		protected String getRedirectUrl(final PageParameter pp) {
			final News news = NewsViewTPage.getNews(pp);
			return news == null ? PAGE404.getUrl() : ((INewsWebContext) newsContext).getUrlsFactory()
					.getUrl(pp, NewsViewPage.class, news, "preview=true");
		}
	}

	public static class RecommendationPage extends AbstractRecommendationPage<News> {

		@Override
		protected INewsService getBeanService() {
			return newsContext.getNewsService();
		}

		@Override
		protected News getBean(final PageParameter pp) {
			return NewsViewTPage.getNews(pp);
		}

		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final JavascriptForward js = super.onSave(cp);
			js.append(createTableRefresh().toString());
			return js;
		}
	}

	public static class StatusDescPage extends AbstractDescPage implements INewsContextAware {

		@Override
		@Transaction(context = INewsContext.class)
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final EContentStatus op = cp.getEnumParameter(EContentStatus.class, "op");
			final INewsService service = newsContext.getNewsService();
			for (final String id : StringUtils.split(cp.getParameter("newsId"), ";")) {
				final News news = service.getBean(id);
				setLogDescription(cp, news);
				news.setStatus(op);
				service.update(new String[] { "status" }, news);
			}
			return super.onSave(cp)
					.append(CategoryTableLCTemplatePage.createTableRefresh().toString());
		}

		@Override
		public String getTitle(final PageParameter pp) {
			final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
			return $m("StatusDescLogPage.0",
					op == EContentStatus.edit ? $m("NewsMgrPage.7") : Convert.toString(op));
		}

		@Override
		protected InputElement createTextarea(final PageParameter pp) {
			final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
			return super.createTextarea(pp).setText(
					$m("StatusDescLogPage.1",
							op == EContentStatus.edit ? $m("NewsMgrPage.7") : Convert.toString(op),
							Convert.toDateString(new Date()), pp.getLogin()));
		}
	}
}
