package com.jalios.jcmsplugin.jbook.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.jalios.jcms.Category;
import com.jalios.jcms.JcmsUtil;
import com.jalios.jcms.Member;
import com.jalios.jcms.Publication;
import com.jalios.jcms.QueryResultSet;
import com.jalios.jcms.comparator.ComparatorManager;
import com.jalios.jcms.handler.QueryHandler;
import com.jalios.jcms.taglib.settings.ControlSettings;
import com.jalios.jcms.taglib.settings.impl.DateSettings;
import com.jalios.jcms.taglib.settings.impl.EnumerateSettings;
import com.jalios.jcms.taglib.settings.impl.MemberSettings;
import com.jalios.jcms.uicomponent.BreadcrumbItem;
import com.jalios.jcms.uicomponent.DataAttribute;
import com.jalios.jcmsplugin.jbook.JBookManager;
import com.jalios.jcmsplugin.jbook.data.JBookBorrowing;
import com.jalios.util.Util;

import generated.Book;

public class JBookAppHandler extends QueryHandler {
	protected QueryResultSet resultSet;
	protected boolean refineWorkspace;
	protected Book book;
	protected Category topic;
	protected Category topicRoot;

	protected boolean showTopics;
	protected JBookManager mgr = JBookManager.getInstance();

	public void init() {
		this.initTopic();
		this.initQuery();
	}

	private void initQuery() {
		this.setTypes(new String[] { "generated.Book" });
		this.setDateType("pdate");
		this.setSort("title");
		if (this.showTopics) {
			this.setExactCat(this.showTopics);
		}

	}

	public QueryResultSet getResultSet() {
		if (this.resultSet == null) {
			this.resultSet = super.getResultSet();
		}

		return this.resultSet;
	}

	public Category getSelectedTopic() {
		return this.topic;
	}

	public boolean showTopics() {
		return this.showTopics;
	}

	public Set getTopicSet() {
		Category topic = this.getSelectedTopic();
		if (topic != null) {
			return topic.getChildrenSet();
		} else {
			Category root = this.mgr.getTopicRoot();
			return root == null ? null : root.getChildrenSet();
		}
	}

	public SortedSet getSortedResultSet() {
		Comparator comparator = ComparatorManager.getComparator(Publication.class, this.getSort());
		return this.getResultSet().getAsSortedSet(comparator);
	}

	public List getBreadcrumbItems() {
		List items = new ArrayList();
		items.add((new BreadcrumbItem()).label(this.glp("jcmsplugin.jbook.app.catalog.root", new Object[0]))
				.url(this.getAppUrl()).attributes((new DataAttribute()).addData("data-jalios-action", "ajax-refresh")));
		List breadcrumb = this.getBreadCrumbCatList();
		if (Util.isEmpty(breadcrumb)) {
			return items;
		} else {
			Iterator var4 = breadcrumb.iterator();

			while (var4.hasNext()) {
				Category cat = (Category) var4.next();
				items.add((new BreadcrumbItem()).label(cat.getName(this.userLang)).url(this.getAppUrl(cat))
						.attributes((new DataAttribute()).addData("data-jalios-action", "ajax-refresh")));
			}

			return items;
		}
	}

	private void initTopic() {
		Set catSet = this.getCategorySet("cids");
		if (Util.isEmpty(catSet)) {
			this.setCids(new String[] { JcmsUtil.getId(this.mgr.getTopicRoot()) });
			catSet = this.getCategorySet("cids");
		}

		if (topicRoot == null) {
			topicRoot = mgr.getTopicRoot();
		}

		this.topic = (Category) Util.getFirst(catSet);
		this.showTopics = Util.isEmpty(this.getText()) && Util.isEmpty(this.getMids()) && this.getBeginDate() == null
				&& this.getEndDate() == null;
	}

	private List getBreadCrumbCatList() {
		Category currentCat = this.getSelectedTopic();
		if (currentCat != null && currentCat != this.topicRoot) {
			List list = currentCat.getAncestorList(this.topicRoot, false);
			Collections.reverse(list);
			list.add(currentCat);
			return list;
		} else {
			return Collections.emptyList();
		}
	}

	public boolean showNoResult() {
		return Util.isEmpty(this.getResultSet());
	}

	public String getAppUrl() {
		return "plugins/JBookPlugin/jsp/app/jbook.jsp";
	}

	public String getAppUrl(Category cat) {
		StringBuilder sb = new StringBuilder(this.getAppUrl());
		sb.append("?");
		if (Util.notEmpty(this.getText())) {
			sb.append("text=" + this.getText() + "&amp;");
		}

		if (cat != null) {
			sb.append("cids=" + cat.getId());
		}

		return sb.toString();
	}

	public MemberSettings getMemberSettings() {
		Member mbr = channel.getMember((String) Util.getFirst(this.getMids()));
		MemberSettings settings = (MemberSettings) ((MemberSettings) ((MemberSettings) ((MemberSettings) (new MemberSettings())
				.name("mids")).value(mbr)).placeholder("jcmsplugin.jbook.app.filter.mbr")).onChange("ajax-refresh");
		if (mbr == null) {
			settings.addOption("hideClearButton", Boolean.TRUE);
		}

		return settings;
	}

	public DateSettings getBeginDateSettings() {
		Date beginDate = this.getBeginDate();
		DateSettings settings = ((DateSettings) ((DateSettings) ((DateSettings) (new DateSettings())
				.name("beginDateStr")).value(beginDate)).placeholder("jcmsplugin.jbook.app.filter.begin-date"))
				.onChange("ajax-refresh");
		if (beginDate == null) {
			settings.addOption("hideClearButton", Boolean.TRUE);
		}

		return settings;
	}

	public DateSettings getEndDateSettings() {
		Date endDate = this.getEndDate();
		DateSettings settings = ((DateSettings) ((DateSettings) ((DateSettings) (new DateSettings()).name("endDateStr"))
				.value(endDate)).placeholder("jcmsplugin.jbook.app.filter.end-date")).onChange("ajax-refresh");
		if (endDate == null) {
			settings.addOption("hideClearButton", Boolean.TRUE);
		}

		return settings;
	}

	public boolean showAddBook() {
		return !this.isLogged ? false : this.loggedMember.canPublishSome(Book.class);
	}

	public String getAddBookUrl() {
		String params = "";
		Category topic = this.getSelectedTopic();
		if (topic != null) {
			params = "?cids=" + topic.getId();
		}

		return "types/Book/editBookModal.jsp" + params;
	}

	public boolean showBook() {
		return this.getSelectedBook() != null;
	}

	public Book getSelectedBook() {
		return this.book;
	}

	public void setBook(String id) {
		this.book = (Book) channel.getData(Book.class, id);
	}

	public String getEditBookUrl() {
		return this.book == null ? null : "types/Book/editBookModal.jsp?id=" + this.book.getId();
	}

	public boolean showAppTitle() {
		return !this.getWorkspace().isCollaborativeSpace();
	}

	public List<JBookBorrowing> getAllBorrowingList() {
		if (refineWorkspace) {
			return mgr.getAllCurrentBorrowingList(getWorkspace());
		}
		return mgr.getAllCurrentBorrowingList();
	}

	public List<JBookBorrowing> getMyBorrowingList() {
		if (refineWorkspace) {
			return mgr.getCurrentBorrowingList(loggedMember, getWorkspace());
		}
		return mgr.getCurrentBorrowingList(loggedMember);
	}

	protected View view = View.CATALOG;

	public enum View {
		CATALOG, MY_BORROWINGS, ALL_BORROWINGS;
	}

	public void setView(String v) {
		try {
			this.view = View.valueOf(v);
		} catch (IllegalArgumentException ignore) {
			// EMPTY
		}
	}

	public String getAppTitle() {
		switch (view) {

		case MY_BORROWINGS:
			return glp("jcmsplugin.jbook.app.view.my-borrowings");

		case ALL_BORROWINGS:
			return glp("jcmsplugin.jbook.app.view.all-borrowings");

		default:
		case CATALOG:
			return glp("jcmsplugin.jbook.app.catalog.title");
		}
	}

	public String getCatalogUrl() {
		return getViewUrl(View.CATALOG);
	}

	public String getMyBorrowingsUrl() {
		return getViewUrl(View.MY_BORROWINGS);
	}

	public String getAllBorrowingsUrl() {
		return getViewUrl(View.ALL_BORROWINGS);
	}

	private String getViewUrl(View view) {
		return getAppUrl() + "?view=" + view;
	}

	public boolean showCatalog() {
		return view == View.CATALOG && !showBook();
	}

	public boolean showMyBorrowings() {
		return view == View.MY_BORROWINGS && !showBook();
	}

	public boolean showAllBorrowings() {
		return view == View.ALL_BORROWINGS && !showBook();
	}

	public ControlSettings getViewSettings() {
		EnumerateSettings settings = new EnumerateSettings().name("view").value(view)
				.enumLabels("jcmsplugin.jbook.app.view.catalog", "jcmsplugin.jbook.app.view.my-borrowings",
						"jcmsplugin.jbook.app.view.all-borrowings")
				.enumValues(View.CATALOG.toString(), View.MY_BORROWINGS.toString(), View.ALL_BORROWINGS.toString())
				.onChange("ajax-refresh");

		return settings;
	}

	public void setRefineWorkspace(String v) {
		refineWorkspace = Util.toBoolean(v, false);
	}

	public void setTopicRoot(Category c) {
		topicRoot = c;
	}

	public Category getTopicRoot() {
		return topicRoot;
	}

	public List<Category> getBreadCrumb() {
		Category currentCat = getSelectedTopic();
		if (currentCat == null || currentCat == topicRoot) {
			return Collections.emptyList();
		}

		List<Category> list = currentCat.getAncestorList(topicRoot, false);
		Collections.reverse(list);

		return list;
	}

}
