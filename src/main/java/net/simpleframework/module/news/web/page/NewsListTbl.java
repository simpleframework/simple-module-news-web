package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.news.bean.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.EVerticalAlign;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;
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
		final ID orgId = NewsUtils.getDomainId(cp);
		if (orgId != null) {
			cp.addFormParameter("orgId", orgId);
		}
		final NewsCategory category = NewsUtils.getNewsCategory(cp);
		if (category != null) {
			cp.addFormParameter("categoryId", category.getId());
		}
		final EContentStatus status = cp.getEnumParameter(EContentStatus.class, "status");
		if (status != null) {
			cp.addFormParameter("status", status.name());
		}
		return _newsService.queryBeans(category, orgId, status, null, null);
	}

	@Override
	protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
		final News news = (News) dataObject;
		final KVMap kv = new KVMap();

		final ImageElement img = createImageMark(cp, news);
		if (img != null) {
			kv.add(TablePagerColumn.ICON, img);
		}
		kv.add("topic", toTopicHTML(cp, news)).add("views", news.getViews())
				.add("comments", toCommentsHTML(cp, news)).add("createDate", news.getCreateDate())
				.add(TablePagerColumn.OPE, toOpeHTML(cp, news));
		return kv;
	}

	@Override
	public Object getRowBeanById(final ComponentParameter cp, final Object id) {
		return _newsService.getBean(id);
	}

	protected ImageElement createImageMark(final ComponentParameter cp, final News news) {
		String img = null;
		if (news.isVideoMark()) {
			img = "mark_video.png";
		} else if (news.isImageMark()) {
			img = "mark_image.png";
		}
		// else if (news.getRecommendation() > 0) {
		// }
		else {
		}
		if (img != null) {
			final ImageElement ele = ImageElement.img16(
					cp.getCssResourceHomePath(NewsListTbl.class) + "/images/" + img).setVerticalAlign(
					EVerticalAlign.middle);
			final String desc = $m("NewsListTbl." + img.substring(0, img.length() - 4));
			if (StringUtils.hasText(desc)) {
				ele.setTitle(desc);
			}
			return ele;
		}
		return null;
	}

	protected String toTopicHTML(final ComponentParameter cp, final News news) {
		final EContentStatus status = cp.getEnumParameter(EContentStatus.class, "status");
		final StringBuilder sb = new StringBuilder();
		final NewsCategory category = NewsUtils.getNewsCategory(cp);
		if (category == null) {
			final NewsCategory category2 = _newsCategoryService.getBean(news.getCategoryId());
			if (category2 != null) {
				final LinkElement le = createCategoryElement(category2);
				sb.append("[").append(le != null ? le : category2.getText()).append("] ");
			}
		}
		if (status == EContentStatus.delete) {
			sb.append(news.getTopic());
		} else {
			final LinkElement le = new LinkElement(news.getTopic());
			if (news.getStatus() == EContentStatus.publish) {
				sb.append(le.setHref(getTopicHref(cp, news)).setTarget("_blank"));
			} else {
				sb.append(le.setOnclick("$Actions['NewsMgrPage_edit']('newsId=" + news.getId() + "');"));
			}
		}
		return sb.toString();
	}

	protected LinkElement createCategoryElement(final NewsCategory category) {
		return new LinkElement(category.getText()).setOnclick(CategoryTableLCTemplatePage
				.createTableRefresh("categoryId=" + category.getId()).toString());
	}

	protected String getTopicHref(final ComponentParameter cp, final News news) {
		return ((INewsWebContext) newsContext).getUrlsFactory().getUrl(cp, NewsViewTPage.class, news);
	}

	protected String toCommentsHTML(final ComponentParameter cp, final News news) {
		final EContentStatus status = cp.getEnumParameter(EContentStatus.class, "status");
		if (status == EContentStatus.delete) {
			return String.valueOf(news.getComments());
		} else {
			return LinkElement.style2(news.getComments())
					.setOnclick("$Actions['NewsMgrPage_commentWindow']('newsId=" + news.getId() + "');")
					.toString();
		}
	}

	protected AbstractElement<?> createPublishBtn(final ComponentParameter cp, final News news) {
		if (news.getStatus() == EContentStatus.edit) {
			return NewsUtils.createStatusAct(cp, EContentStatus.publish, news);
		} else {
			return new ButtonElement($m("Edit")).setOnclick("$Actions['NewsMgrPage_edit']('newsId="
					+ news.getId() + "');");
		}
	}

	protected String toOpeHTML(final ComponentParameter cp, final News news) {
		final StringBuilder sb = new StringBuilder();
		final EContentStatus status = cp.getEnumParameter(EContentStatus.class, "status");
		if (status != EContentStatus.delete) {
			sb.append(createPublishBtn(cp, news));
			sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
		} else {
			sb.append(NewsUtils.createStatusAct(cp, EContentStatus.edit, news).setText(
					$m("NewsListTbl.0")));
		}
		return sb.toString();
	}

	@Override
	public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
			final MenuItem menuItem) {
		if (menuItem == null) {
			final MenuItems items = MenuItems
					.of(MenuItem.of($m("AbstractContentBean.2")).setOnclick_act(
							"NewsMgrPage_recommendation", "newsId"))
					.append(MenuItem.sep())
					.append(MenuItem.itemEdit().setOnclick_act("NewsMgrPage_edit", "newsId"))
					.append(
							MenuItem.itemDelete().setOnclick_act("NewsMgrPage_status", "newsId",
									"op=" + EContentStatus.delete.name())).append(MenuItem.sep())
					.append(MenuItem.itemLog().setOnclick_act("NewsMgrPage_update_log", "newsId"));
			return items;
		}
		return null;
	}

	public static final TablePagerColumn TC_TOPIC() {
		return new TablePagerColumn("topic", $m("NewsMgrPage.1")).setSort(false);
	}

	public static final TablePagerColumn TC_VIEWS() {
		return new TablePagerColumn("views", $m("NewsMgrPage.2"), 70).setPropertyClass(Float.class);
	}

	public static final TablePagerColumn TC_COMMENTS() {
		return new TablePagerColumn("comments", $m("NewsMgrPage.3"), 50)
				.setTextAlign(ETextAlign.center);
	}

	public static final TablePagerColumn TC_CREATEDATE() {
		return TablePagerColumn.DATE("createDate", $m("NewsMgrPage.4"));
	}
}
