package org.jdownloader.plugins.components.youtube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.appwork.utils.StringUtils;
import org.appwork.utils.logging2.extmanager.Log;
import org.jdownloader.plugins.components.youtube.itag.YoutubeITAG;
import org.jdownloader.plugins.components.youtube.variants.AbstractVariant;
import org.jdownloader.plugins.components.youtube.variants.DescriptionVariantInfo;
import org.jdownloader.plugins.components.youtube.variants.SubtitleVariant;
import org.jdownloader.plugins.components.youtube.variants.SubtitleVariantInfo;
import org.jdownloader.plugins.components.youtube.variants.VariantBase;
import org.jdownloader.plugins.components.youtube.variants.VariantInfo;
import org.jdownloader.plugins.components.youtube.variants.YoutubeSubtitleStorable;
import org.jdownloader.settings.staticreferences.CFG_YOUTUBE;

import jd.plugins.DownloadLink;

public class YoutubeClipData {

    /**
     *
     */

    public String                                    user;
    public String                                    channel;
    public long                                      date;
    public String                                    error;
    public boolean                                   ageCheck;
    public String                                    title;
    public String                                    videoID;
    public int                                       playlistEntryNumber;
    public int                                       length;
    public String                                    category;
    public int                                       duration;
    public String                                    channelID;
    public long                                      dateUpdated;
    public String                                    userGooglePlusID;
    public YoutubeITAG                               bestVideoItag;
    public String                                    description;
    public Map<YoutubeITAG, List<YoutubeStreamData>> streams;
    public ArrayList<YoutubeSubtitleStorable>        subtitles;
    public HashMap<String, String>                   keywords3D;
    public HashSet<String>                           keywords;
    public String                                    approxThreedLayout;

    public YoutubeClipData(final String videoID) {
        this(videoID, -1);
    }

    /**
     * Checks the keywords and guesses if this is a sbs or hou 3d video, but not declared as 3d by youtube
     *
     * @return
     */
    public boolean guessSBSorHOU3D() {
        if (keywords != null) {

            StringBuilder sb = new StringBuilder();
            for (String s : keywords) {
                sb.append(" ").append(s.toLowerCase(Locale.ENGLISH));

            }
            if (title != null) {
                sb.append(" ").append(title.toLowerCase(Locale.ENGLISH));
            }
            if (description != null) {
                sb.append(" ").append(description.toLowerCase(Locale.ENGLISH));
            }

            String str = sb.toString();
            if (str.contains("3d")) {
                if (str.contains("sbs")) {
                    return true;
                }
                if (str.contains("side") && str.contains("by")) {
                    return true;
                }

                if (str.contains("hou")) {
                    return true;
                }
                if (str.contains("cardboard")) {
                    return true;
                }
            }

        }
        return false;
    }

    public Projection getProjection() {
        if (is3D()) {
            return Projection.ANAGLYPH_3D;
        }
        int highestProjection = -1;
        for (Entry<YoutubeITAG, List<YoutubeStreamData>> s : streams.entrySet()) {
            for (YoutubeStreamData stream : s.getValue()) {
                highestProjection = Math.max(highestProjection, stream.getProjectionType());
            }
        }
        int threeDLayout = -1;
        try {
            threeDLayout = approxThreedLayout == null ? -1 : Integer.parseInt(approxThreedLayout);
        } catch (Throwable e) {

        }
        if (highestProjection == 2 && threeDLayout != 3) {
            return Projection.SPHERICAL;
        }
        if (highestProjection == 3) {
            return Projection.SPHERICAL_3D;
        }
        if (highestProjection == 2 && threeDLayout == 3) {
            return Projection.SPHERICAL_3D;
        }
        if (guessSBSorHOU3D()) {
            return Projection.ANAGLYPH_3D;
        }
        return Projection.NORMAL;
    }

    private boolean is3D() {
        // from yt player js
        if ("1".equals(approxThreedLayout)) {
            return true;
        }
        if (keywords != null) {
            if (keywords.contains("3D")) {
                return true;
            }

        }
        if (keywords3D != null) {
            if (StringUtils.equals(keywords3D.get("enable"), "true")) {
                return true;
            }
            if (StringUtils.equals(keywords3D.get("enable"), "LR")) {
                return true;
            }
            if (StringUtils.equals(keywords3D.get("enable"), "RL")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return videoID + "/" + title;
    }

    public YoutubeClipData(final String videoID, final int playlistEntryNumber) {

        this.videoID = videoID;
        this.playlistEntryNumber = playlistEntryNumber;
    }

    public void copyToDownloadLink(final DownloadLink thislink) {
        thislink.setProperty(YoutubeHelper.YT_TITLE, title);
        thislink.setProperty(YoutubeHelper.YT_PLAYLIST_INT, playlistEntryNumber);
        thislink.setProperty(YoutubeHelper.YT_3D, is3D());
        thislink.setProperty(YoutubeHelper.YT_CHANNEL, channel);
        thislink.setProperty(YoutubeHelper.YT_USER, user);
        thislink.setProperty(YoutubeHelper.YT_BEST_VIDEO, bestVideoItag == null ? null : bestVideoItag.name());
        thislink.setProperty(YoutubeHelper.YT_DATE, date);
        thislink.setProperty(YoutubeHelper.YT_LENGTH_SECONDS, length);
        thislink.setProperty(YoutubeHelper.YT_GOOGLE_PLUS_ID, userGooglePlusID);
        thislink.setProperty(YoutubeHelper.YT_CHANNEL_ID, channelID);
        thislink.setProperty(YoutubeHelper.YT_DURATION, duration);
        thislink.setProperty(YoutubeHelper.YT_DATE_UPDATE, dateUpdated);
        thislink.getTempProperties().setProperty(YoutubeHelper.YT_DESCRIPTION, description);

    }

    public List<YoutubeStreamData> getStreams(YoutubeITAG itag) {
        if (itag == null) {
            return null;
        }
        List<YoutubeStreamData> ret = streams.get(itag);
        if (ret == null) {
            // check alternatives
            List<YoutubeITAG> alternatives = YoutubeITAG.getTagList(itag.getITAG());
            if (alternatives != null) {
                for (YoutubeITAG tag : alternatives) {
                    ret = streams.get(tag);
                    if (ret != null) {
                        break;
                    }
                }
            }
        }
        return ret;
    }

    public List<VariantInfo> findSubtitleVariants() {
        List<VariantInfo> allSubtitles = new ArrayList<VariantInfo>();

        for (final YoutubeSubtitleStorable si : subtitles) {

            SubtitleVariantInfo vi = new SubtitleVariantInfo(new SubtitleVariant(si), this);
            allSubtitles.add(vi);

        }

        return allSubtitles;
    }

    public ArrayList<VariantInfo> findDescriptionVariant() {
        ArrayList<VariantInfo> descriptions = new ArrayList<VariantInfo>();

        final String descText = description;
        if (StringUtils.isNotEmpty(descText)) {

            VariantInfo vi;

            descriptions.add(vi = new DescriptionVariantInfo(descText, this));

        }
        return descriptions;
    }

    public List<VariantInfo> findVariants() {
        ArrayList<VariantInfo> ret = new ArrayList<VariantInfo>();

        for (VariantBase v : VariantBase.values()) {

            if (!CFG_YOUTUBE.CFG.isExternMultimediaToolUsageEnabled() && v.isVideoToolRequired()) {

                continue;
            }
            if (!v.isValidFor(this)) {
                Log.info("Invalid Variant for: " + v);

                continue;
            }
            // System.out.println("test for " + v);

            List<YoutubeStreamData> audio = null;
            List<YoutubeStreamData> video = null;
            List<YoutubeStreamData> data = null;
            boolean valid = v.getiTagVideo() != null || v.getiTagAudio() != null || v.getiTagData() != null;

            if (v.getiTagVideo() != null) {
                video = streams.get(v.getiTagVideo());
                if (video == null) {
                    valid = false;
                }
            }
            if (v.getiTagAudio() != null) {
                audio = streams.get(v.getiTagAudio());
                if (audio == null) {
                    valid = false;
                }
            }
            if (v.getiTagData() != null) {
                data = streams.get(v.getiTagData());
                if (data == null) {
                    valid = false;
                }
            }

            if (valid) {

                VariantInfo vi = new VariantInfo(AbstractVariant.get(v, this, audio, video, data), audio, video, data);
                ret.add(vi);

            }
        }
        return ret;
    }

}