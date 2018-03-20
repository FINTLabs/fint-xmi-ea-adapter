package no.fint.provider.eaxmi.service;

import org.kohsuke.github.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class GitHubService {

    GHRepository repo;

    @PostConstruct
    public void init() {

        try {
            GitHub github = GitHub.connect();
            repo = github.getRepository("fint-informasjonsmodell"); // hent fint-informasjonsmodell.xml

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public GHContent getFileFromBranch(String path, String branchName) {
        try {
            GHBranch branch = repo.getBranch(branchName);
            getGHFileContent(path, branch.getSHA1());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    public GHContent getFileFromRelease(String path, String tagName) {
        return null;
    }

    private GHContent getGHFileContent(String path, String sha) {
        try {
            PagedIterable iterable = repo.listTags();
            return repo.getFileContent(path, sha);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }
}
