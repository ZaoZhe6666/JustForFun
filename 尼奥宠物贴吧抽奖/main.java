package random;
import java.util.ArrayList;
import java.util.Collections;

public class main {
	private static ArrayList<participant> list = new ArrayList<participant>();
	public static void init(){
		list.add(new participant("shenxianlingzhi", "生日卡多提"));
		list.add(new participant("fengzhongqiyuan", "雪兔"));
		list.add(new participant("golalago", "紫色笔刷"));
		list.add(new participant("chenshu133","Mosaic Negg"));
		list.add(new participant("banbanleelee\t","海盗艾莎药水"));
		list.add(new participant("卜\t","冬日毛衣围巾"));
		list.add(new participant("futsuki","泡泡龙身上现扒下来的衣服"));
		list.add(new participant("jimaaa70","精灵巴兹药水"));
		list.add(new participant("yvan316","石头"));
		list.add(new participant("joshua_imba","坏了的尼奥点数"));
		list.add(new participant("dfhyying","棍鞋裙杖饰品中的一个"));
		list.add(new participant("ice_alkaid","分色笔刷"));
		list.add(new participant("elven_fu\t","可爱的r99"));
		list.add(new participant("riverbanksoul","黄石公园"));
		return;
	}
	public static void Congratulate(int Send, int Receive){
		System.out.println("恭喜！！！");
		System.out.println("ID为 " + list.get(Receive).getID() + "\t的幸运儿得到了这件礼物————\t" + list.get(Send).getGift());	
		return;
	}
	public static void gosleep(){
		//吊人胃口的卡顿时间
		try {
			Thread.sleep((int)(Math.random()*3000%3000 + 1500));
		} catch (InterruptedException e) {
		}
		return;
	}
	public static void garbage(int type){
		switch(type){
		case 0:{
			System.out.println("哇！");
			gosleep();
			System.out.println("会是谁的呢！");
			gosleep();
			System.out.println("真是让人期待！");
			gosleep();
			break;
		}
		case 1:{
			System.out.println("幸运儿是谁呢！");
			gosleep();
			System.out.println("他就是……");
			gosleep();
			System.out.println("他就是！！！");
			gosleep();
			break;
		}
		case 2:{
			System.out.println("这个奖品会花落谁家呢？");
			gosleep();
			break;
		}
		case 3:{
			System.out.println("这个奖品非常丰厚呢！");
			gosleep();
			System.out.println("那么这个奖品会归谁呢！");
			gosleep();
			System.out.println("答案即将揭晓……");
			gosleep();
			break;
		}
		}
		return;
	}
	public static void main(String args[]) {
		System.out.println("数据初始化中...");
		//假装初始化很耗时间
		gosleep();
		//初始化ArrayList内容
		init();
		//乱序ArrayList
		Collections.shuffle(list);
		
		int people_num = list.size();
		System.out.println("激动人心的大抽奖开始啦！！");
		gosleep();
		for(int i = 0; i < people_num; i++){
			System.out.println("第"+(i+1)+"件礼物是：");
			gosleep();
			System.out.print("当当当当！\t");
			System.out.println("\t\t"+list.get(i).getGift());
			System.out.println("");
			gosleep();
			//输出一句垃圾话
			garbage((int)(Math.random()*4%4));
			int randnum;
			//抽签部分
			do{
				randnum = (int) (Math.random()*people_num % people_num);
				if(randnum == i){
					continue;
				}
			}while(list.get(randnum).have_got_present());
			list.get(i).set_send2(randnum);
			//输出恭喜
			Congratulate(i,randnum);
			//装模作样的卡顿几秒
			gosleep();
			gosleep();
			System.out.println("\n\n");
		}
		System.out.println("全部抽奖已经结束啦");
		gosleep();
		System.out.println("那么汇总一下最终的抽奖结果");
		for(int i = 0; i < people_num; i++){
			System.out.println(list.get(i).getID()+"\t---- "+list.get(i).getGift()+" ---->\t"+list.get(list.get(i).get_send2()).getID());
		}
		return;
	}
}
class participant{
	private String ID;
	private String Gift;
	private boolean got_present;
	private int send2;
	public participant(String ID, String Gift){
		this.ID = ID;
		this.Gift = Gift;
		this.got_present = false;
	}
	public String getID(){
		return this.ID;
	}
	public String getGift(){
		return this.Gift;
	}
	public boolean have_got_present(){
		if(this.got_present){
			return true;
		}
		this.got_present = true;
		return false;
	}
	public void set_send2(int order){
		this.send2 = order;
	}
	public int get_send2(){
		return this.send2;
	}
}
