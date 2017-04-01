package tk.nihanth.pcp_coordinator.Models;

public class NewsItem {

    public String NewsTitle;
    public String News;
    public String CourseId;
    public String CentreId;

    public String getNewsTitle() {
        return NewsTitle;
    }

    public String getNewsMsg() {
        return News;
    }

    public NewsItem(String newsTitle, String news, String courseId, String centreId) {
        NewsTitle = newsTitle;
        News = news;
        CourseId = courseId;
        CentreId = centreId;
    }
}
