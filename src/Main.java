import com.hyunjae.mannaspace.Post;
import com.hyunjae.mannaspace.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        workTest();
    }

    public static void workIteratorTest() throws Exception {
        Work work = new Work("https://manaa.space/work/rPvJXZOPbkz57aj4");
        Iterator<String> thumbnailIterator = work.getThumbnails().iterator();
        Iterator<String> dateIterator = work.getDates().iterator();
        Iterator<String> issueNumberIterator = work.getIssueNumbers().iterator();
        Iterator<String> urlIterator = work.getUrls().iterator();
        while(thumbnailIterator.hasNext() && dateIterator.hasNext() && issueNumberIterator.hasNext() && urlIterator.hasNext())
            logger.debug("thumbnail: {} date: {} issueNumber: {} url: {}", thumbnailIterator.next(), dateIterator.next(), issueNumberIterator.next(), urlIterator.next());
    }

    public static void workTest() throws Exception {
        Work work = new Work("https://manaa.space/work/rPvJXZOPbkz57aj4");
        logger.debug(
                "author: {}\n" +
                "name: {}\n" +
                "thumbnail: {}\n" +
                "thumbnails: {}\n" +
                "dates: {}\n" +
                "issue-numbers: {}\n" +
                "urls: {}\n" +
                "toString: {}\n",
                work.getAuthor(),
                work.getName(),
                work.getThumbnail(),
                work.getThumbnails(),
                work.getDates(),
                work.getIssueNumbers(),
                work.getUrls(),
                work.toString()
        );
    }

    public static void postTest() throws Exception {
        Post post = new Post("https://manaa.space/post/uploader/XjaYZqoN1bl6pRDv");
        logger.debug(
                "images: {}\n" +
                "author: {}\n" +
                "date-published: {}\n" +
                "issue-number: {}\n" +
                "name: {}\n" +
                "series-name: {}\n" +
                "thumbnail: {}\n" +
                "toString: {}",
                post.getImages(),
                post.getAuthor(),
                post.getDatePublished(),
                post.getIssueNumber(),
                post.getName(),
                post.getSeriesName(),
                post.getTumbnail(),
                post.toString()
        );
    }
}
