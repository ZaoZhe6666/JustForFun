package random;
import java.util.ArrayList;
import java.util.Collections;

public class main {
	private static ArrayList<participant> list = new ArrayList<participant>();
	public static void init(){
		list.add(new participant("shenxianlingzhi", "���տ�����"));
		list.add(new participant("fengzhongqiyuan", "ѩ��"));
		list.add(new participant("golalago", "��ɫ��ˢ"));
		list.add(new participant("chenshu133","Mosaic Negg"));
		list.add(new participant("banbanleelee\t","������ɯҩˮ"));
		list.add(new participant("��\t","����ë��Χ��"));
		list.add(new participant("futsuki","�����������ְ��������·�"));
		list.add(new participant("jimaaa70","�������ҩˮ"));
		list.add(new participant("yvan316","ʯͷ"));
		list.add(new participant("joshua_imba","���˵���µ���"));
		list.add(new participant("dfhyying","��Ьȹ����Ʒ�е�һ��"));
		list.add(new participant("ice_alkaid","��ɫ��ˢ"));
		list.add(new participant("elven_fu\t","�ɰ���r99"));
		list.add(new participant("riverbanksoul","��ʯ��԰"));
		return;
	}
	public static void Congratulate(int Send, int Receive){
		System.out.println("��ϲ������");
		System.out.println("IDΪ " + list.get(Receive).getID() + "\t�����˶��õ���������������\t" + list.get(Send).getGift());	
		return;
	}
	public static void gosleep(){
		//����θ�ڵĿ���ʱ��
		try {
			Thread.sleep((int)(Math.random()*3000%3000 + 1500));
		} catch (InterruptedException e) {
		}
		return;
	}
	public static void garbage(int type){
		switch(type){
		case 0:{
			System.out.println("�ۣ�");
			gosleep();
			System.out.println("����˭���أ�");
			gosleep();
			System.out.println("���������ڴ���");
			gosleep();
			break;
		}
		case 1:{
			System.out.println("���˶���˭�أ�");
			gosleep();
			System.out.println("�����ǡ���");
			gosleep();
			System.out.println("�����ǣ�����");
			gosleep();
			break;
		}
		case 2:{
			System.out.println("�����Ʒ�Ứ��˭���أ�");
			gosleep();
			break;
		}
		case 3:{
			System.out.println("�����Ʒ�ǳ�����أ�");
			gosleep();
			System.out.println("��ô�����Ʒ���˭�أ�");
			gosleep();
			System.out.println("�𰸼�����������");
			gosleep();
			break;
		}
		}
		return;
	}
	public static void main(String args[]) {
		System.out.println("���ݳ�ʼ����...");
		//��װ��ʼ���ܺ�ʱ��
		gosleep();
		//��ʼ��ArrayList����
		init();
		//����ArrayList
		Collections.shuffle(list);
		
		int people_num = list.size();
		System.out.println("�������ĵĴ�齱��ʼ������");
		gosleep();
		for(int i = 0; i < people_num; i++){
			System.out.println("��"+(i+1)+"�������ǣ�");
			gosleep();
			System.out.print("����������\t");
			System.out.println("\t\t"+list.get(i).getGift());
			System.out.println("");
			gosleep();
			//���һ��������
			garbage((int)(Math.random()*4%4));
			int randnum;
			//��ǩ����
			do{
				randnum = (int) (Math.random()*people_num % people_num);
				if(randnum == i){
					continue;
				}
			}while(list.get(randnum).have_got_present());
			list.get(i).set_send2(randnum);
			//�����ϲ
			Congratulate(i,randnum);
			//װģ�����Ŀ��ټ���
			gosleep();
			gosleep();
			System.out.println("\n\n");
		}
		System.out.println("ȫ���齱�Ѿ�������");
		gosleep();
		System.out.println("��ô����һ�����յĳ齱���");
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
