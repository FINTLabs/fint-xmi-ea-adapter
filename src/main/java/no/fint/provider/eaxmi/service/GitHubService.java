package no.fint.provider.eaxmi.service;

import org.kohsuke.github.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Service
public class GitHubService {

    GHRepository repo;

    @PostConstruct
    public void init() {

        try {
            GitHub github = GitHub.connect("stigto", "ab2160cf9bf9e597e0e7a2b1");
            repo = github.getRepository("fint-informasjonsmodell"); // Får IndexOutOfBoundsException her fordi noe Spring annotation-greier.

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // hent fint-informasjonsmodell.xml

    public GHContent getFileFromBranch(String path, String branchName) {
        try {
            GHBranch branch = repo.getBranch(branchName);
            getGHFileContent(path, branch.getSHA1());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    public GHContent getFileFromTag(String path, String tagName) {
        if (null == tagName) {
            return null;
        }
        try {
            List<GHTag> tags = repo.listTags().asList();
            for(GHTag tag : tags) {
                if (tagName.equals(tag.getName())) {
                    return repo.getFileContent(path, tag.getCommit().getSHA1());
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private GHContent getGHFileContent(String path, String sha) {
        try {
            return repo.getFileContent(path, sha);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }
}
