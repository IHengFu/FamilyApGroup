package com.changhong.wifiairscout.ui.activity;

import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.db.DBHelper;
import com.changhong.wifiairscout.model.DeviceLocation;

/**
 * Created by fuheng on 2017/12/20.
 */

public class TestActivity extends BaseActivtiy {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test);

        ImageView imageView = findViewById(R.id.imageView_test);
        Drawable drawable = imageView.getDrawable();
//        drawable = DrawableCompat.wrap(drawable).mutate();
//        imageView.setImageDrawable(drawable);
//        drawable.setTint(Color.BLUE);
        DrawableCompat.setTint(drawable,Color.YELLOW);
        Toast.makeText(this,drawable.getClass().getName(),Toast.LENGTH_LONG).show();
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            AnimatedVectorDrawableCompat d = ((AnimatedVectorDrawableCompat) drawable);
            d.start();
        }

        String content = "奶爸的文艺人生 654 第654章 什么是龟儿子啊？（1/4）\n" +
                "\n" +
                "　　跟杨轶两口子一样，等着看《士兵突击》这部开年大戏的观众不在少数，许多杨轶的书迷，还有很多央视电视剧的忠实观众，甚至一些被亲朋好友们的讨论给吸引的观众都坐在了电视机前。\n" +
                "\n" +
                "　　当然，也有例外，比如楚勤，他是一名普通的公交车司机，忙碌了一天下来，回到家里，唯一的念头就是泡杯热茶，泡泡脚，然后舒舒服服地看一场足球比赛。\n" +
                "\n" +
                "　　八岁的儿子被他培养的，也是对足球产生了浓烈的兴趣，这不，楚勤在看电视的时候，这小子也兴冲冲地跑过来，挤在爸爸的身边要一起看。\n" +
                "\n" +
                "　　“作业写完了吗？”楚勤问道。\n" +
                "\n" +
                "　　“写完了！”这小子，目不转睛、不假思索地回答。\n" +
                "\n" +
                "　　不过，今天这场亚冠的比赛开场比较早，楚勤打开电视的时候已经是下半场踢到一半了，没一会儿，比赛结束。\n" +
                "\n" +
                "　　“没了，去看书吧！”楚勤拍了拍儿子的脑袋，笑道。\n" +
                "\n" +
                "　　“再找找！”小家伙不死心，不知道是看不过瘾，还是想逃避学习。\n" +
                "\n" +
                "　　楚勤优哉游哉地喝着茶，看儿子在按着遥控器，他是知道的，没有别的比赛，今天所有联赛都要为亚足协的比赛让路，而欧洲那边、国内有直播的比赛，还要等两个小时之后才开场。\n" +
                "\n" +
                "　　不过，小家伙按着遥控器，不知不觉来到了中央台，一段似乎是在吟诵的音乐吸引了他的注意，按到下一台的手指不由地停了下来。\n" +
                "\n" +
                "　　这是什么？楚勤也忍不住瞅了两眼，结果就挪不开眼了。\n" +
                "\n" +
                "　　《征服天堂》的音乐，配上《士兵突击》的片头，有着别样的魔力，让人在平淡中感到那种难以抑制的热血沸腾，让人在心潮澎湃中体验着生与死的悲壮。（注1）\n" +
                "\n" +
                "　　尤其是片头一群战士在雨中，抱着钢枪嘶喊着往前冲锋，而音乐里也是低沉的呜鸣，仿佛有种让人忍不住要站起来向他们致敬的冲动！\n" +
                "\n" +
                "　　随着背景音乐的高扬，奇怪的人声吟唱，配合强健有力的鼓声，越来越嘹亮的号声，战士们在铁丝网泥坑里匍匐前行、在密集丛林里奔跑、扛着木头跑步，那一个个拼命的表情，令人肃然起敬。\n" +
                "\n" +
                "　　这是什么片子？\n" +
                "\n" +
                "　　楚勤跟全国许多无意间按到这个台的路人观众一样，产生了好奇心。\n" +
                "\n" +
                "　　小家伙害怕他爹骂他，看了一会儿，醒悟过来，赶紧继续换台找足球比赛。\n" +
                "\n" +
                "　　“别，按回刚才那个台！”楚勤看得津津有味呢，结果一闪没了，他连忙叫儿子换回来，“没有足球比赛了，就看这个吧！”\n" +
                "\n" +
                "　　小家伙偷偷瞅了一眼老爹的表情，看到没有叫他去看书的意思，这才安心地往后一靠，跟他爹一样，有模有样地翘起二郎腿，看起了电视。\n" +
                "\n" +
                "　　“《士兵突击》？这是什么片子？”片头过后，楚勤才知道自己看的是什么电视剧，居然还是第一集！\n" +
                "\n" +
                "　　他儿子反而听了这话，眼睛一亮，说道：“爸爸，我知道，这是杨轶写的一本书，我同桌跟我说过，我可喜欢杨轶写的故事书了！”\n" +
                "\n" +
                "　　“你在学校看故事书？”楚勤望了过来，眼神里露出了危险的信号。\n" +
                "\n" +
                "　　小家伙这才意识到自己不小心说漏了嘴，瞠目结舌地不知道该怎么回应。\n" +
                "\n" +
                "　　这对父子一边斗嘴一边看电视剧，倒也是看得津津有味。\n" +
                "\n" +
                "　　不过跟他们不一样，很多观众看《士兵突击》，是想看陈风尘拍的故事，跟他们看过的原著是否一致，是否也一样精彩！\n" +
                "\n" +
                "　　比如远在羊城的穆钰诚，作为鸽子王杨轶的铁杆粉丝，杨轶的作品被搬上电视，穆钰诚怎么会错过？\n" +
                "\n" +
                "　　“杨轶大大生了大胖儿子之后，半年没有发新书，只能看看他的老作品解解闷。”穆钰诚长吁短叹地看着电视。\n" +
                "\n" +
                "　　《士兵突击》这部电视剧，剧情发展跟穆钰诚记忆中的原著差不多，穆钰诚比较在意的是演员们的演技，比如许三多的扮演者耿厦！\n" +
                "\n" +
                "　　“都说许三多是新人演员演的，这个耿厦以前还是跑龙套的，也算是逆袭了！”穆钰诚看着片头，暗暗嘀咕着，“不知道演得怎么样！对了，这个片头谁做的？这么牛批？看得我大晚上的热血沸腾？”\n" +
                "\n" +
                "　　不过，电视剧开始之后，镜头给到耿厦，穆钰诚看到耿厦不高的个子、憨厚的面孔，就隐隐觉得这小子还不错！\n" +
                "\n" +
                "　　剧情快速地推进着，才六分多钟，穆钰诚看到了迷彩打扮的“许三多”在断了的高架桥上搭木板的一幕。\n" +
                "\n" +
                "　　“卧槽，傻蛋，别冒险啊！你这是演习，要摔下去的！”穆钰诚竟然忍不住叫出了声。\n" +
                "\n" +
                "　　尽管知道最后许三多没有死，可是穆钰诚还是忍不住为扮演者耿厦捏了一把汗。\n" +
                "\n" +
                "　　不知不觉中，穆钰诚已经融入了这个故事里，仿佛许三多就是耿厦，耿厦不是在演许三多，他就是许三多一般！\n" +
                "\n" +
                "　　说不出是耿厦演得好，还是陈风尘这个老油条拍戏拍得好，每一个镜头，每一个剪辑，都恰到好处地起到了煽情的效果！\n" +
                "\n" +
                "　　“他娘的！”穆钰诚看到耿厦流血的脸庞无力地耷拉向一边的时候，忍不住眼中都跳出了泪花，他抹了一把眼泪，骂了起来，“你以为我不知道，你死不了吗？你这家伙，命跟小强一样！”\n" +
                "\n" +
                "　　骂，那是因为感动……\n" +
                "\n" +
                "　　镜头回到了许三多小的时候，故事进程很快，几个镜头，便从许三多备受欺负的童年，过渡到了许三多长大之后，当然，期间两个哥哥去应征被刷的故事，也都用一种诙谐的叙事方式呈现给了观众。\n" +
                "\n" +
                "　　这些镜头还是很搞笑的，只是搞笑中，那家徒四壁的家境，那拮据、无奈、粗鲁的老爹，还有三个不中用的儿子，让人感到一种莫名的心酸。\n" +
                "\n" +
                "　　“拍得是真的好！让我自己想，我可想不到这么苦……”穆钰诚都沉默地看着，心里感慨万千。\n" +
                "\n" +
                "　　原本他也就想看看许三多的扮演者演得怎么样，差不多就关电视去玩游戏了，可是，这一看，穆钰诚就停不下来了。\n" +
                "\n" +
                "　　回到公交车司机楚勤的家里，楚勤看着史班长到村里家访的一幕幕，看着淳朴又带着一点狡猾劲儿的农民，看着灰蒙蒙、土黄土黄的环境，看着人们老土的衣着打扮，不由地陷入了沉思。\n" +
                "\n" +
                "　　他也是农民的孩子，这一幕幕，让他想起了他的过去。\n" +
                "\n" +
                "　　楚勤现在也只是城市的底层小人物，可是，相比起小时候的艰苦，他现在至少环境整洁，过上了有好衣裳穿、有好饭菜吃的生活。\n" +
                "\n" +
                "　　如果自己这个熊儿子跟许三多一样，出生在农村，也傻乎乎的啥也不懂，别说踢球了，自己会不会也让他去当兵呢？\n" +
                "\n" +
                "　　“爸爸！”他念头里的熊儿子正在叫着他。\n" +
                "\n" +
                "　　“什么？”楚勤回过了神。\n" +
                "\n" +
                "　　“为什么许三多他爸爸要踢许三多啊？爸爸你打我，都不会用脚的！”小家伙问道。\n" +
                "\n" +
                "　　楚勤乐了，问道：“你想让我踹你吗？”\n" +
                "\n" +
                "　　“不要，我又不是球！”小家伙哇哇地叫了起来。\n" +
                "\n" +
                "　　人家看电视剧是看剧情，这家伙，看电视剧是看热闹啊！\n" +
                "\n" +
                "　　楚勤笑了笑，接着看下去，他已经被这剧情给吸引了。一开始是许三多那么悲壮地在战争中掩护队友陷入危机，现在又是在说许三多还没当兵前的故事。\n" +
                "\n" +
                "　　楚勤很想知道，这个矮个子、没有什么优点、还很笨的小子，后来是怎么当上兵的。\n" +
                "\n" +
                "　　但这个时候，他儿子又问了：“爸！”\n" +
                "\n" +
                "　　“什么？”\n" +
                "\n" +
                "　　“龟儿子是什么意思啊？”\n" +
                "\n" +
                "　　“……”";
        ((TextView) findViewById(R.id.text_test)).setText(content);
    }
}
