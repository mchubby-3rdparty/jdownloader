package jd.plugins.components;

/**
 * used for defining template types. Use by testclass
 *
 * @author raztoki
 *
 */
public class SiteType {

    public static enum SiteTemplate {

        /**
         * Sold on: <a href="http://www.digitaldutch.com/arles/">digitaldutch.com</a><br />
         * examples <a href="http:/palcomix.com/">palcomix.com</a>
         */
        ArlesImageWebPageCreator,

        /**
         * As the name says this class contains code to handle redirect/linkcrypt sites which have no direct redirects. Basically it is for
         * websites for which a single class would be too much as only 1-2 lines of code are needed to handle them. <b>Main decrypter
         * class:</b> DecrypterForRedirectServicesWithoutDirectRedirects<br />
         * Example: <a href="http://migre.me/">migre.me</a>
         */
        DecrypterForRedirectServicesWithoutDirectRedirects,

        /**
         * <a href="http://gempixel.com/project/premium-url-shortener/">Premium URL Shortener</a><br />
         * sold on <a href="http://codecanyon.net/item/premium-url-shortener/3688135">codecanyon.net</a><br />
         * examples <a href="http://cehs.ch/">cehs.ch</a> <a href="http://www.csurl.it">csurl.it</a>
         *
         */
        GemPixel_PremiumURLShortener,

        /**
         * Script for link anonymizers.<br />
         * <b>Example that suits main decrypter class:</b> <a href="http:/l.moapi.net/">l.moapi.net</a><br />
         * <b>Main decrypter class:</b> GeneralLinkAnonymizer<br />
         * <b>Requirements to be added to main class:</b> CryptedLink must NOT be accessed - final links can be build using the information
         * we have in the urls which the user added.<br />
         */
        GeneralLinkAnonymizer,

        /**
         * Decrypter to auto download- and add these linkcontainers: DLC, RSDF, CCF<br />
         * <b>Main decrypter class:</b> GenericAutoContainer<br />
         */
        GenericAutoContainer,

        /**
         * Script for URLs containing base64 strings which lead to downloadlinks.<br />
         * <b>Example that suits main decrypter class:</b> <a href="http:/free.downloader.my/">free.downloader.my</a><br />
         * <b>Main decrypter class:</b> GenericBase64Decrypter<br />
         * <b>Requirements to be added to main class:</b> CryptedLink must NOT be accessed - final links can be build using the information
         * we have in the urls which the user added.<br />
         */
        GenericBase64Decrypter,

        /**
         * Script for .m38u (master) hls URLs.<br />
         * <b>Main decrypter class:</b> GenericM3u8Decrypter<br />
         */
        GenericM3u8Decrypter,

        /**
         * Script to be used for all kinds of direct-redirect (http response 302) websites.<br />
         * <b>Main decrypter class:</b> Rdrctr<br />
         * Example: <a href="http://www.smarturl.it/">smarturl.it</a>
         */
        GeneralRedirectorDecrypter,

        /**
         * Script used by some video/porn hosting sites.<br />
         * Can be bought e.g. from here: <a href="http://www.kernel-video-sharing.com/en/"</a> >kernel-video-sharing.com</a>.<br />
         * Demo: <a href="http://www.kvs-demo.com/">kvs-demo.com</a>
         */
        KernelVideoSharing,

        /**
         * Script used by some image hosting sites e.g.: <a href="http:/damimage.com/">damimage.com</a>. <br />
         * Can be bought e.g. from here: <a href="http://codecanyon.net/item/imgshot-image-hosting-script/2558257"
         * >http://codecanyon.net/item/imgshot-image-hosting- script/2558257</a>.<br />
         * <b>Main decrypter class:</b> ImgShotDecrypt<br />
         * <b>Example that suits main decrypter class:</b> <a href="http:/imgshot.com/">imgshot.com</a><br />
         * <b>Example that does NOT suit main decrypter class (needs separate host class):</b> <a href="http:/imgbar.net/">imgbar.net</a> <br />
         * Official Demo: <a href="http://imgshot.com/">imgshot.com</a><br />
         */
        ImageHosting_ImgShot,

        /**
         * Should cover all given templates.<br />
         * <a href="http://sibsoft.net/xfilesharing.html">XFileSharing<a><br />
         * <a href="http://sibsoft.net/xfilesharing_free.html">XFileSharing FREE (old version)</a><br />
         * <a href="http://sibsoft.net/xvideosharing.html">XVideoSharing<a><br />
         * <a href="http://sibsoft.net/ximagesharing.html">XImageSharing<a><br />
         */
        SibSoft_XFileShare,

        /**
         * <a href="http://sibsoft.net/xlinker.html">XLinker</a>
         */
        SibSoft_XLinker,

        /**
         * <a href="https://mfscripts.com/yetishare/overview.html">YetiShare - PHP File Hosting Site Script</a>
         */
        MFScripts_YetiShare,

        /**
         * <a href="https://mfscripts.com/wurlie/overview.html">Wurlie - PHP Short Url Script</a>
         */
        MFScripts_Wurlie,

        /**
         * <a href="http://www.hostedtube.com/">hosted tube</a> porn script/template provided by <a
         * href="http://pimproll.com/">pimproll.com</a>
         */
        PimpRoll_HostedTube,

        /**
         * MultiUpload script by unknown, called Qooy Mirrors (taken from paypal description) <a href="http://qooy.com/sale.php">Qooy
         * Mirrors</a>
         */
        Qooy_Mirrors,

        /**
         * Script used by some chinese file-hosting sites. <a href="http:/sx566.com/">sx566.com</a>. Not sure what to call this script.
         */
        Unknown_ChineseFileHosting,
        /**
         * Script used by some multihosters e.g. <a href="http://www.premiumax.net/">premiumax.net</a>. Not sure what to call this script.
         */
        Unknown_MultihosterScript,

        /**
         * the template that supports mirror stack type sites.. Not sure what to call this script.
         */
        Unknown_MirrorStack,

        /**
         * <b>Minimum requirements:</b> Usage of "flowplayer" <br />
         * <b>Additional requirements to be added to main class:</b> NO account support<br />
         * <b>Main host class:</b> UnknownPornScript1<br />
         * <b>Example that suits main host class:</b> <a href="http:/dansmovies.com/">dansmovies.com</a><br />
         * <b>Example that does NOT suit main host class (needs separate class):</b> -<br />
         * <b>Example that needs a decrypter class :</b> -<br />
         */
        UnknownPornScript1,

        /**
         * <b>Minimum requirements:</b> Website has to belong to that specified porn network/company <br />
         * <b>Additional requirements to be added to main class:</b> NO account support<br />
         * <b>Main host class:</b> UnknownPornScript2<br />
         * <b>Example that suits main host class:</b> <a href="http:/pornmaki.com/">pornmaki.com</a><br />
         * <b>Example that does NOT suit main host class (needs separate class):</b> -<br />
         * <b>Example that needs a decrypter class :</b> -<br />
         */
        UnknownPornScript2,

        /**
         * <b>Minimum requirements:</b> Website has to fit script <br />
         * <b>Additional requirements to be added to main class:</b> NO account support<br />
         * <b>Main host class:</b> UnknownPornScript3<br />
         * <b>Example that suits main host class:</b> <a href="http:/xxxkinky.com/">xxxkinky.com</a><br />
         * <b>Example that does NOT suit main host class (needs separate class):</b> -<br />
         * <b>Example that needs a decrypter class :</b> -<br />
         */
        UnknownPornScript3,

        /**
         * <b>Minimum requirements:</b> Website has to fit script <br />
         * <b>Additional requirements to be added to main class:</b> NO account support<br />
         * <b>Main host class:</b> UnknownPornScript4<br />
         * <b>Example that suits main host class:</b> <a href="http:/pornyeah.com/">pornyeah.com</a><br />
         * <b>Example that does NOT suit main host class (needs separate class):</b> <a href="http:/eroxia.com/">eroxia.com</a><br />
         * <b>Example that needs a decrypter class :</b> <a href="http:/eroxia.com/">eroxia.com</a><br />
         */
        UnknownPornScript4,

        /**
         * <b>Minimum requirements:</b> Usage of "jwplayer" <br />
         * <b>Additional requirements to be added to main class:</b> Usage of "jwplayer" - one of the 2 basic jwplayer js/html sources, NO
         * account support<br />
         * <b>Main host class:</b> UnknownPornScript5<br />
         * <b>Example that suits main host class:</b> <a href="http:/boyfriendtv.com/">boyfriendtv.com</a><br />
         * <b>Example that does NOT suit main host class [needs separate class]:</b> <a href="http:/pornhd.com/">pornhd.com</a><br />
         * <b>Example that needs a decrypter class :</b> -<br />
         */
        UnknownPornScript5,

        /**
         * <b>Minimum requirements:</b> Website has to fit script <br />
         * <b>Additional requirements to be added to main class:</b> NO account support<br />
         * <b>Main host class:</b> -<br />
         * <b>Example that suits main host class:</b> -<br />
         * <b>Example that does NOT suit main host class [needs separate class]:</b> <a href="http:/fux.com/">fux.com</a><br />
         * <b>Example that needs a decrypter class :</b> -<br />
         */
        UnknownPornScript6,

        /**
         * <b>Minimum requirements:</b> Website has to fit script <br />
         * <b>Additional requirements to be added to main class:</b> -<br />
         * <b>Main host class:</b> -<br />
         * <b>Example that suits main host class:</b> -<br />
         * <b>Example that does NOT suit main host class [needs separate class]:</b> <a href="http:/foxytube.com/">foxytube.com</a><br />
         * <b>Example that needs a decrypter class :</b> -<br />
         */
        UnknownPornScript7,

        /**
         * Script used by some video hosting sites. <a href="http://cloudy.ec/">cloudy.ec</a>. Not sure what to call this script.
         */
        Unknown_VideoHosting,

        /**
         * Turbobit hosted sites. <a href="http://turbobit.net/">turbobit.net</a>
         */
        Turbobit_Turbobit,

        /**
         * linkbucks hosted sites. <a href="http://linkbucks.com/">linkbucks.com</a>
         */
        Linkbucks_Linkbucks,

        /**
         * safelinking hosted sites <a href="http://safelinking.net/">safelinking.net</a>
         */
        SafeLinking_SafeLinking,

        /**
         * image board. <a href="https://github.com/r888888888/danbooru">danbooru github project</a>
         */
        Danbooru;

    }

}
