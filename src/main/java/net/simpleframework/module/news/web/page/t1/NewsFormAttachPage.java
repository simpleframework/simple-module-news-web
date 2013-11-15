package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.FileUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.web.NewsLogRef.NewsDownloadLogPage;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.DownloadUtils;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/form/attach")
public class NewsFormAttachPage extends NewsFormBasePage {

	@Override
	protected void addComponents(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pp,
				"NewsTabAttachPage_tbl", TablePagerBean.class)
				.setPagerBarLayout(EPagerBarLayout.bottom).setContainerId("tbl_" + hashId)
				.setHandleClass(NewsAttachmentTbl.class);
		tablePager
				.addColumn(
						new TablePagerColumn("topic", $m("NewsFormAttachPage.0"))
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("attachsize", $m("NewsFormAttachPage.1"), 80)
								.setTextAlign(ETextAlign.left))
				.addColumn(new TablePagerColumn("downloads", $m("NewsFormAttachPage.2"), 80))
				.addColumn(new TablePagerColumn("userId", $m("NewsFormAttachPage.3"), 100))
				.addColumn(new TablePagerColumn("createDate", $m("NewsFormAttachPage.4"), 120))
				.addColumn(TablePagerColumn.OPE().setWidth(120));

		addComponentBean(pp, "NewsTabAttachPage_logPage", AjaxRequestBean.class).setUrlForward(
				url(NewsDownloadLogPage.class));
		addComponentBean(pp, "NewsTabAttachPage_logWin", WindowBean.class)
				.setContentRef("NewsTabAttachPage_logPage").setHeight(480).setWidth(800)
				.setTitle($m("NewsFormAttachPage.5"));
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='NewsFormAttachPage' id='tbl_").append(hashId).append("'></div>");
		return sb.toString();
	}

	public static class NewsAttachmentTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final News news = context.getNewsService().getBean(cp.getParameter("newsId"));
			if (news != null) {
				cp.addFormParameter("newsId", news.getId());
				return context.getAttachmentService().queryByContent(news);
			}
			return null;
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final Attachment attachment = (Attachment) dataObject;
			final KVMap kv = new KVMap();
			try {
				final AttachmentFile af = context.getAttachmentService().createAttachmentFile(
						attachment);
				kv.put(
						"topic",
						new LinkElement(attachment.getTopic()).setOnclick("$Actions.loc('"
								+ DownloadUtils.getDownloadHref(af) + "');"));
			} catch (final IOException e) {
				kv.put("topic", attachment.getTopic());
			}
			kv.put("attachsize", FileUtils.toFileSize(attachment.getAttachsize()));
			kv.put("downloads", new ButtonElement(attachment.getDownloads())
					.setOnclick("$Actions['NewsTabAttachPage_logWin']('beanId=" + attachment.getId()
							+ "');"));
			kv.put("userId", cp.getUser(attachment.getUserId()));
			kv.put("createDate", attachment.getCreateDate());
			final StringBuilder ope = new StringBuilder();
			ope.append(ButtonElement.editBtn());
			ope.append(SpanElement.SPACE);
			ope.append(ButtonElement.deleteBtn());
			kv.add(TablePagerColumn.OPE, ope);
			return kv;
		}
	}
}
