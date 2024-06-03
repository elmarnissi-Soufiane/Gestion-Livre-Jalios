package com.jalios.jcmsplugin.jbook.data;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.jalios.jcms.Content;
import com.jalios.jcms.Member;
import com.jalios.jcmsplugin.jbook.JBookManager;

public abstract class AbstractBook extends Content {

	private transient JBookManager mgr = JBookManager.getInstance();

	public AbstractBook() {
		// EMPTY
	}

	public JBookBorrowing getCurrentBorrowing() {
		return mgr.getCurrentBorrowing(this);
	}

	public List<JBookBorrowing> getBorrowingList(int firstResult, int maxResults) {
		return mgr.getBorrowingList(this);
	}

	public Set<Member> getPreviousBorrowerSet() {
		return mgr.getPreviousBorrowerSet(this);
	}

	public AbstractBook(AbstractBook other) {
		super(other);
	}

	public String getAppDisplayUrl(Locale userLocale) {
		return JBookManager.getInstance().getAppUrlPrefix(workspace, userLocale) + "book=" + getId();
	}

	abstract public String getDescription(String lang);

	abstract public String getImage();
}
