package io.jenkins.plugins.sample;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.DiffEntry;

public class TakeDiffOnBranch implements Action {

   private String name;
   private List<String> diffs;

   public TakeDiffOnBranch(String name, String selectedBranch, FilePath path, TaskListener listener) {
       this.name = name + ".git will be download";

       try { List<String> selectedDiffs =
           takeDiff(name, selectedBranch, path, listener);

           this.diffs = selectedDiffs;
       } catch (GitAPIException e) {
           System.err.println(e);
       } catch (IOException io) {
           System.err.println(io);
       }


  }

  public static List<String> takeDiff(String name, String selectedBranch, FilePath path, TaskListener listener) throws InvalidRemoteException, TransportException, GitAPIException,
    IOException {

      List<String> newDiffs = new ArrayList<String>();
      File clonedRepoPath = new File(path + "/repo");
      String repo = "https://github.com/" + name + ".git";

      deleteDir(clonedRepoPath);

      listener.getLogger().println("Repo " + name + ".git download into " + clonedRepoPath );


      try (Git git = Git.cloneRepository()
          .setURI(repo)
          .setDirectory(clonedRepoPath)
          .setBranch(selectedBranch)
          .call()){

          listener.getLogger().println("Switch to branch: " + selectedBranch );

          ObjectReader reader = git.getRepository().newObjectReader();

          CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
          ObjectId oldTree = git.getRepository().resolve( "HEAD~1^{tree}" );
          oldTreeIter.reset( reader, oldTree );

          CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
          ObjectId newTree = git.getRepository().resolve( "HEAD^{tree}" );
          newTreeIter.reset( reader, newTree );

          DiffFormatter diffFormatter = new DiffFormatter( DisabledOutputStream.INSTANCE );
          diffFormatter.setRepository( git.getRepository() );
          List<DiffEntry> entries = diffFormatter.scan( oldTreeIter, newTreeIter );

          for( DiffEntry entry : entries ) {

              newDiffs.add(entry.getNewPath());
          }
          diffFormatter.close();
      }

      return newDiffs ;
  }



  public static boolean deleteDir(File dir) {
      if (dir.isDirectory()) {
          String[] children = dir.list();
          for (int i=0; i<children.length; i++) {
              boolean success = deleteDir(new File(dir, children[i]));
              if (!success) {
                  return false;
              }
          }
      }
      return dir.delete();
  }

  public String getName() {
      return name;
  }

  public List<String> getDiffs() {
      return diffs;
  }

  @Override
  public String getIconFileName() {
      return null;
  }

  @Override
  public String getDisplayName() {
      return null;
  }

  @Override
  public String getUrlName() {
      return null;
  }
}

