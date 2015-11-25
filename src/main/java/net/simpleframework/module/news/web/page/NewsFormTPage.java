package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.simpleframework.common.StringUtils;
import net.simpleframework.common.object.ObjectUtils;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.common.web.html.HtmlUtils.IElementVisitor;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.ContentException;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.common.web.content.ContentUtils;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsLogRef.NewsAttachmentAction;
import net.simpleframework.module.news.web.page.mgr2.NewsMgrTPage;
import net.simpleframework.module.news.web.page.t1.NewsFormBasePage;
import net.simpleframework.module.news.web.page.t1.NewsMgrPage;
import net.simpleframework.module.news.web.page.t2.NewsViewPage;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.common.element.TextButton;
import net.simpleframework.mvc.component.AbstractComponentBean;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ext.attachments.AttachmentBean;
import net.simpleframework.mvc.component.ext.attachments.AttachmentUtils;
import net.simpleframework.mvc.component.ext.attachments.IAttachmentSaveCallback;
import net.simpleframework.mvc.component.ext.category.ICategoryHandler;
import net.simpleframework.mvc.component.ext.ckeditor.HtmlEditorBean;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryBean;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryTreeHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsFormTPage extends FormTableRowTemplatePage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addFormValidationBean(pp).addValidators(
				new Validator(EValidatorMethod.required, "#ne_topic, #ne_categoryText, #ne_content"));

		// Html编辑器
		addHtmlEditorBean(pp).setTextarea("ne_content");

		// 类目字典
		addComponentBean(pp, "NewsForm_dict_tree", TreeBean.class).setHandlerClass(
				CategorySelectedTree.class);
		addComponentBean(pp, "NewsForm_dict", DictionaryBean.class).setBindingId("ne_categoryId")
				.setBindingText("ne_categoryText").addTreeRef(pp, "NewsForm_dict_tree")
				.setTitle($m("NewsFormTPage.1")).setHeight(320);

		// 上传
		addComponentBean(pp, "NewsForm_upload_page", AttachmentBean.class).setInsertTextarea(
				"ne_content").setHandlerClass(NewsAttachmentAction.class);
		addComponentBean(pp, "NewsForm_upload", WindowBean.class)
				.setContentRef("NewsForm_upload_page").setTitle($m("NewsFormTPage.10")).setPopup(true)
				.setHeight(480).setWidth(400);
	}

	protected boolean isHtmlEditorCodeEnabled() {
		return false;
	}

	protected HtmlEditorBean addHtmlEditorBean(final PageParameter pp) {
		String attachClick = "$Actions['NewsForm_upload']('" + OPT_VIEWER + "=' + $F('" + OPT_VIEWER
				+ "')";
		final News news = NewsViewTPage.getNews(pp);
		if (news != null) {
			attachClick += " + '&newsId=" + news.getId() + "'";
		}
		attachClick += ");";
		return (HtmlEditorBean) addHtmlEditorBean(pp, "NewsForm_editor", isHtmlEditorCodeEnabled())
				.setAttachAction(attachClick).setToolbar(ContentUtils.HTML_TOOLBAR_BASE)
				.setHeight("340");
	}

	protected News createNews(final PageParameter pp) {
		final News news = _newsService.createBean();
		news.setCreateDate(new Date());
		news.setUserId(pp.getLoginId());
		news.setDomainId(NewsUtils.getDomainId(pp));
		return news;
	}

	@Transaction(context = INewsContext.class)
	@Override
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		final NewsCategory category = _newsCategoryService.getBean(cp.getParameter("ne_categoryId"));
		if (category == null) {
			throw ContentException.of($m("NewsFormTPage.9"));
		}

		final String ne_cname = cp.getParameter("ne_cname");
		News news = _newsService.getBean(cp.getParameter("ne_id"));
		final boolean insert = (news == null);
		if (insert) {
			news = createNews(cp);
		} else {
			if (!ObjectUtils.objectEquals(news.getCname(), ne_cname)
					&& _newsService.getBeanByName(ne_cname) != null) {
				throw ContentException.of($m("NewsFormTPage.7") + ne_cname);
			}
		}

		news.setCategoryId(category.getId());
		news.setCname(ne_cname);
		news.setTopic(cp.getParameter("ne_topic"));
		news.setKeyWords(cp.getParameter("ne_keyWords"));
		news.setSource(cp.getParameter("ne_source"));
		news.setAuthor(cp.getParameter("ne_author"));

		final Document doc = HtmlUtils.createHtmlDocument(cp.getParameter("ne_content"));
		news.setContent(doNewsContent(cp, news, doc));

		news.setDescription(cp.getParameter("ne_description"));
		news.setAllowComments(cp.getBoolParameter(OPT_ALLOWCOMMENTS));
		news.setIndexed(cp.getBoolParameter(OPT_INDEXED));
		news.setImageMark(cp.getBoolParameter(OPT_IMAGEMARK));

		final News news2 = news;
		final ComponentParameter nCP = ComponentParameter.get(cp,
				cp.getComponentBeanByName("NewsForm_upload_page"));
		AttachmentUtils.doSave(nCP, new IAttachmentSaveCallback() {
			@Override
			public void save(final Map<String, AttachmentFile> addQueue, final Set<String> deleteQueue)
					throws IOException {
				final IAttachmentService<Attachment> aService = newsContext.getAttachmentService();
				if (insert) {
					_newsService.insert(news2);
				} else {
					_newsService.update(news2);
					if (deleteQueue != null) {
						aService.delete(deleteQueue.toArray(new Object[] { deleteQueue.size() }));
					}
				}
				aService.insert(news2.getId(), cp.getLoginId(), addQueue.values());
			}
		});
		return doSaveForward(cp, news);
	}

	protected JavascriptForward doSaveForward(final ComponentParameter cp, final News news) {
		final StringBuilder js = new StringBuilder();
		js.append(
				((INewsWebContext) newsContext).getUrlsFactory().getUrl(cp, NewsFormBasePage.class,
						news)).append("&op=save");
		final String url = cp.getParameter("url");
		if (StringUtils.hasText(url)) {
			js.append("&url=").append(HttpUtils.encodeUrl(url));
		}
		return new JavascriptForward(JS.loc(js.toString()));
	}

	public String doNewsContent(final PageParameter pp, final News news, final Document doc) {
		final ArrayList<IElementVisitor> al = new ArrayList<IElementVisitor>();
		al.add(HtmlUtils.REMOVE_TAG_VISITOR("script"));
		al.add(HtmlUtils.STRIP_CONTEXTPATH_VISITOR(pp.request));
		if (pp.getBoolParameter(OPT_REMOVE_CLASS)) {
			setVisitor_removeClass(news, al);
		}
		if (pp.getBoolParameter(OPT_REMOVE_STYLE)) {
			setVisitor_removeStyle(news, al);
		}
		if (pp.getBoolParameter(OPT_REMOVE_TAG_FONT)) {
			setVisitor_removeTagFont(news, al);
		}
		if (pp.getBoolParameter(OPT_TARGET_BLANK)) {
			setVisitor_targetBlank(news, al);
		}
		return HtmlUtils.doDocument(doc, al.toArray(new IElementVisitor[al.size()])).html();
	}

	protected void setVisitor_targetBlank(final News news, final List<IElementVisitor> al) {
		al.add(HtmlUtils.TARGET_BLANK_VISITOR);
	}

	protected void setVisitor_removeClass(final News news, final List<IElementVisitor> al) {
		al.add(HtmlUtils.REMOVE_ATTRI_VISITOR("class"));
	}

	protected void setVisitor_removeStyle(final News news, final List<IElementVisitor> al) {
		al.add(HtmlUtils.REMOVE_ATTRI_VISITOR("style"));
	}

	protected void setVisitor_removeTagFont(final News news, final List<IElementVisitor> al) {
		al.add(HtmlUtils.REMOVE_TAG_VISITOR("font", true));
	}

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		final InputElement ne_id = InputElement.hidden("ne_id");
		final InputElement ne_topic = new InputElement("ne_topic");
		final InputElement ne_categoryId = InputElement.hidden("ne_categoryId");
		final TextButton ne_categoryText = new TextButton("ne_categoryText")
				.setOnclick("$Actions['NewsForm_dict']()");

		final InputElement ne_keyWords = new InputElement("ne_keyWords");
		final InputElement ne_source = new InputElement("ne_source");
		final InputElement ne_author = new InputElement("ne_author");

		final InputElement ne_description = InputElement.textarea("ne_description").setRows(3);
		final InputElement ne_content = InputElement.textarea("ne_content")
				.addStyle("display: none;");

		final InputElement ne_cname = new InputElement("ne_cname");

		NewsCategory category = null;
		final News news = NewsViewTPage.getNews(pp);
		if (news != null) {
			ne_id.setText(news.getId());
			ne_cname.setText(news.getCname());
			ne_topic.setText(news.getTopic());
			ne_keyWords.setText(news.getKeyWords());
			ne_source.setText(news.getSource());
			ne_author.setText(news.getAuthor());
			ne_content.setText(ContentUtils.getContent(pp, newsContext.getAttachmentService(), news));
			ne_description.setText(news.getDescription());
			category = _newsCategoryService.getBean(news.getCategoryId());
		} else {
			final String _ne_cname = pp.getParameter("ne_cname");
			if (StringUtils.hasText(_ne_cname)) {
				ne_cname.setText(_ne_cname);
			}
		}
		if (category == null) {
			category = _newsCategoryService.getBean(pp.getParameter("categoryId"));
		}
		if (category != null) {
			ne_categoryId.setText(category.getId());
			ne_categoryText.setText(category.getText());
		}

		final TableRow r1 = new TableRow(
				new RowField($m("NewsFormTPage.0"), ne_id, ne_topic).setStarMark(true), new RowField(
						$m("NewsFormTPage.1"), ne_categoryId, ne_categoryText).setElementsStyle(
						"width:150px;").setStarMark(true));
		if (pp.isLmanager()) {
			// 唯一名称，保留给系统管理员
			r1.append(new RowField($m("NewsFormTPage.13"), ne_cname).setElementsStyle("width:150px;"));
		}
		final TableRow r2 = new TableRow(new RowField($m("NewsFormTPage.2"), ne_keyWords),
				new RowField($m("NewsFormTPage.3"), ne_source).setElementsStyle("width:150px;"),
				new RowField($m("NewsFormTPage.4"), ne_author).setElementsStyle("width:150px;"));
		final TableRow r3 = new TableRow(
				new RowField($m("NewsFormTPage.5"), ne_content).setStarMark(true));
		final TableRow r4 = new TableRow(new RowField($m("NewsFormTPage.6"), ne_description));
		final TableRows rows = TableRows.of(r1, r2, r3, r4);
		return rows;
	}

	public static final String OPT_ALLOWCOMMENTS = "opt_allowComments";

	public static final String OPT_INDEXED = "opt_indexed";

	public static final String OPT_IMAGEMARK = "opt_imageMark"; // 图片新闻

	//
	public static final String OPT_VIEWER = "opt_viewer";

	public static final String OPT_TARGET_BLANK = "opt_targetBlank";

	public static final String OPT_REMOVE_CLASS = "opt_removeClass";

	public static final String OPT_REMOVE_STYLE = "opt_removeStyle";

	public static final String OPT_REMOVE_TAG_FONT = "opt_removeTagFont";

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final Checkbox opt_allowComments = new Checkbox(OPT_ALLOWCOMMENTS, $m("NewsFormTPage.8"));
		final Checkbox opt_indexed = new Checkbox(OPT_INDEXED, $m("NewsFormTPage.14"));
		final Checkbox opt_imageMark = new Checkbox(OPT_IMAGEMARK, $m("NewsFormTPage.11"));

		final Checkbox opt_viewer = new Checkbox(OPT_VIEWER, $m("NewsFormTPage.12")).setChecked(true);

		final Checkbox opt_targetBlank = new Checkbox(OPT_TARGET_BLANK, $m("NewsFormTPage.16"))
				.setChecked(true);
		final Checkbox opt_removeClass = new Checkbox(OPT_REMOVE_CLASS, $m("NewsFormTPage.17"))
				.setChecked(true);
		final Checkbox opt_removeStyle = new Checkbox(OPT_REMOVE_STYLE, $m("NewsFormTPage.18"));
		final Checkbox opt_removeTagFont = new Checkbox(OPT_REMOVE_TAG_FONT, $m("NewsFormTPage.19"));

		final News news = NewsViewTPage.getNews(pp);
		if (news != null) {
			opt_allowComments.setChecked(news.isAllowComments());
			opt_indexed.setChecked(news.isIndexed());
			opt_imageMark.setChecked(news.isImageMark());
		} else {
			final News _news = new News();
			opt_allowComments.setChecked(_news.isAllowComments());
			opt_indexed.setChecked(_news.isIndexed());
			opt_imageMark.setChecked(_news.isImageMark());
		}

		final ElementList el = ElementList.of();
		el.append(opt_allowComments)
				.append(SpanElement.SPACE)
				.append(opt_imageMark)
				.append(SpanElement.SPACE)
				.append(opt_indexed)
				.append(SpanElement.SPACE)
				.append(
						new LinkButton($m("NewsFormTPage.15"))
								.setOnclick("$('idNewsForm_opts').toggle();"))
				.append(
						new BlockElement()
								.setId("idNewsForm_opts")
								.addStyle("display: none;")
								.addElements(opt_viewer, SpanElement.SPACE, opt_targetBlank,
										SpanElement.SPACE, opt_removeClass, SpanElement.SPACE,
										opt_removeStyle, SpanElement.SPACE, opt_removeTagFont));
		return el;
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final News news = NewsViewTPage.getNews(pp);
		final ElementList el = ElementList.of();
		if (news != null) {
			el.append(new ButtonElement($m("Button.Preview")).setOnclick(JS.loc(
					((INewsWebContext) newsContext).getUrlsFactory().getUrl(pp, NewsViewPage.class,
							news, "preview=true"), true)), SpanElement.SPACE);
		}
		el.append(SAVE_BTN());
		return el;
	}

	@Override
	public boolean isButtonsOnTop(final PageParameter pp) {
		return true;
	}

	@Override
	public int getLabelWidth(final PageParameter pp) {
		return 70;
	}

	private AbstractComponentBean categoryBean;

	protected TreeNodes getCategoryDictTreenodes(final ComponentParameter cp, final TreeNode parent) {
		if (categoryBean == null) {
			categoryBean = get(NewsMgrPage.class).getCategoryBean();
			if (categoryBean == null) {
				categoryBean = get(NewsMgrTPage.class).getComponentBeanByName(cp, "NewsMgrTPage_tree");
			}
		}
		final ComponentParameter nCP = ComponentParameter.get(cp, categoryBean);
		final ICategoryHandler cHandle = (ICategoryHandler) nCP.getComponentHandler();
		return cHandle.getCategoryDictTreenodes(nCP, (TreeBean) cp.componentBean, parent);
	}

	public static class CategorySelectedTree extends DictionaryTreeHandler {

		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			return ((NewsFormTPage) get(cp)).getCategoryDictTreenodes(cp, parent);
		}
	}
}
