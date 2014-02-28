package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.FileUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsLogRef.NewsDownloadLogPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.DownloadUtils;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/form/attach")
public class NewsFormAttachPage extends NewsFormBasePage {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pp,
				"NewsTabAttachPage_tbl", TablePagerBean.class)
				.setPagerBarLayout(EPagerBarLayout.bottom).setContainerId("tbl_" + hashId)
				.setHandlerClass(NewsAttachmentTbl.class);
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

		if (((INewsWebContext) context).getLogRef() != null) {
			// 下载日志
			addComponentBean(pp, "NewsTabAttachPage_logPage", AjaxRequestBean.class).setUrlForward(
					url(NewsDownloadLogPage.class));
			addComponentBean(pp, "NewsTabAttachPage_logWin", WindowBean.class)
					.setContentRef("NewsTabAttachPage_logPage").setHeight(480).setWidth(800)
					.setTitle($m("NewsFormAttachPage.5"));
		}

		// 删除
		addDeleteAjaxRequest(pp, "NewsFormAttachPage_delete");

		// 编辑
		addAjaxRequest(pp, "NewsFormAttachPage_editPage", AttachmentEditPage.class);
		addWindowBean(pp, "NewsFormAttachPage_edit").setContentRef("NewsFormAttachPage_editPage")
				.setHeight(240).setWidth(420).setTitle($m("AttachmentEditPage.0"));
	}

	@Transaction(context = INewsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		if (ids != null) {
			context.getAttachmentService().delete(ids);
		}
		return new JavascriptForward("$Actions['NewsTabAttachPage_tbl']();");
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
			final Object id = attachment.getId();
			try {
				final AttachmentFile af = context.getAttachmentService().createAttachmentFile(
						attachment);
				kv.put(
						"topic",
						new LinkElement(attachment.getTopic()).setOnclick(
								"$Actions.loc('" + DownloadUtils.getDownloadHref(af) + "');").setTitle(
								attachment.getDescription()));
			} catch (final IOException e) {
				kv.put("topic", attachment.getTopic());
			}
			kv.put("attachsize", FileUtils.toFileSize(attachment.getAttachsize()));
			if (((INewsWebContext) context).getLogRef() != null) {
				kv.put("downloads", new ButtonElement(attachment.getDownloads())
						.setOnclick("$Actions['NewsTabAttachPage_logWin']('beanId=" + id + "');"));
			} else {
				kv.put("downloads", attachment.getDownloads());
			}
			kv.put("userId", cp.getUser(attachment.getUserId()));
			kv.put("createDate", attachment.getCreateDate());
			final StringBuilder ope = new StringBuilder();
			ope.append(ButtonElement.editBtn().setOnclick(
					"$Actions['NewsFormAttachPage_edit']('beanId=" + id + "');"));
			ope.append(SpanElement.SPACE);
			ope.append(ButtonElement.deleteBtn().setOnclick(
					"$Actions['NewsFormAttachPage_delete']('id=" + id + "');"));
			kv.add(TablePagerColumn.OPE, ope);
			return kv;
		}
	}

	public static class AttachmentEditPage extends FormTableRowTemplatePage {

		@Transaction(context = INewsContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final IAttachmentService<Attachment> aService = context.getAttachmentService();
			final Attachment attachment = getCacheBean(cp, aService, "beanId");
			if (attachment != null) {
				attachment.setTopic(cp.getParameter("ae_topic"));
				attachment.setDescription(cp.getParameter("ae_description"));
				aService.update(new String[] { "topic", "description" }, attachment);
			}
			final JavascriptForward js = super.onSave(cp);
			js.append("$Actions['NewsTabAttachPage_tbl']();");
			return js;
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final InputElement beanId = InputElement.hidden("beanId");
			final InputElement ae_topic = new InputElement("ae_topic");
			final InputElement ae_description = InputElement.textarea("ae_description").setRows(4);

			final Attachment attachment = getCacheBean(pp, context.getAttachmentService(), "beanId");
			if (attachment != null) {
				beanId.setText(attachment.getId());
				ae_topic.setText(attachment.getTopic());
				ae_description.setText(attachment.getDescription());
			}
			return TableRows.of(new TableRow(
					new RowField($m("AttachmentEditPage.1"), beanId, ae_topic)), new TableRow(
					new RowField($m("Description"), ae_description)));
		}
	}
}
