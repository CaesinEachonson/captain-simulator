package org.captainsim.chapter;

public class ChapterCommand {
    private final String chapterMaster;
    private final String chiefApothecary;
    private final String chiefChaplain;
    private final String chiefLibrarian;
    private final String masterOfTheForge;

    public ChapterCommand(String chapterMaster, String chiefApothecary,
                          String chiefChaplain, String chiefLibrarian,
                          String masterOfTheForge) {
        this.chapterMaster = chapterMaster;
        this.chiefApothecary = chiefApothecary;
        this.chiefChaplain = chiefChaplain;
        this.chiefLibrarian = chiefLibrarian;
        this.masterOfTheForge = masterOfTheForge;
    }

    // getters...
    public String getChapterMaster() { return chapterMaster; }
    public String getChiefApothecary() { return chiefApothecary; }
    public String getChiefChaplain() { return chiefChaplain; }
    public String getChiefLibrarian() { return chiefLibrarian; }
    public String getMasterOfTheForge() { return masterOfTheForge; }
}
