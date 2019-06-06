#coding:UTF-8
import win32api
import win32con
import win32gui
import time
import os
import random
from PIL import ImageGrab
from PIL import Image

dict = {
	# Click中C中心位置坐标
	'click_x' : 176,
	'click_y' : 251,

	# 刷新键中心位置坐标
	'refreshx' : 107,
	'refreshy' : 69,

	# CC auto字样中，第一个大写C中央位置坐标
	'cc_auto_x' : 613,
	'cc_auto_y' : 562,

	# 拉杆后，No中的o中央位置坐标
	'no_x' : 448,
	'no_y' : 627,

	# 物品柜物品数
	'item_num' : 20,

	# hash值表
	'roll_page_hash' : '01c8000006800ee01ff03ff83ff87ffc7cde3f3c1e761f001fb01ff83fffffff',
	'handle_hash' : 'e0c7c1f381f980fc00fc003800000000000000008000c001e000f00ffe7fffff',
	'confirm_hash' : 'ffff00000000ffffffffffffffff80018001fffffffffffff81ff81ff81ff81f',
	'yes_hash' : '00000000000000001a001e001ff80df00ff80d180df000000000000000000000',
	'success_hash' : 'ffff00000000ffff00000000f7ffffffffffffffffffffffffffffffffffffff',
	'cc_btn_hash' : 'ffffa7ff07f707f76ff76e406e006f006f046e046e046e0406040600a640b640',
	'monkey_say_hash' : '07ff03ff03ff023f03ff03ff03ff02eb02634bff5bffdbffdbff4bff4bff07ff',
	'block_hash' : '3ef7ffff26c626c6ffffffffffffffffffffffffffffffffffffffffffffffff'
}

def move_click(x, y, t = 0):  # 移动鼠标并点击左键
	win32api.SetCursorPos((x, y))  # 设置鼠标位置(x, y)
	if t == 0:
		win32api.mouse_event(win32con.MOUSEEVENTF_LEFTDOWN | win32con.MOUSEEVENTF_LEFTUP, x, y, 0, 0)  # 点击鼠标左键

def get_handle_page():  # 获取把手页面窗口信息
	wdname = u'Living with Less - Google Chrome'
	handle = win32gui.FindWindow(None, wdname)  # 获取窗口句柄
	if handle == 0:
		print_to_file("Error to find Page\" Living with Less \"")
		return None
	else:
		return win32gui.GetWindowRect(handle)

def get_item_page():  # 获取物品柜页面窗口信息
	wdname = u'Neopets - Inventory - Google Chrome'
	handle = win32gui.FindWindow(None, wdname)  # 获取窗口句柄
	if handle == 0:
		print_to_file("Error to find Page\" Neopets - Inventory \"")
		return None
	else:
		return win32gui.GetWindowRect(handle)

def get_hash(img):		# 对图片计算hash值
	img = img.resize((16, 16), Image.ANTIALIAS).convert('L')  # 抗锯齿 灰度
	avg = sum(list(img.getdata())) / 256  # 计算像素平均值
	s = ''.join(map(lambda i: '0' if i < avg else '1', img.getdata()))  # 每个像素进行比对,大于avg为1,反之为0
	return ''.join(map(lambda j: '%x' % int(s[j:j+4], 2), range(0, 256, 4)))

def hamming(hash1, hash2, n=20):	# 计算汉明距离
	b = False
	assert len(hash1) == len(hash2)
	if sum(ch1 != ch2 for ch1, ch2 in zip(hash1, hash2)) < n:
		b = True
	return b

def random_in_range(min, max):		# 在给定范围内进行随机数
	return min + int((max - min) * random.random())

def pic_grab(topx, topy, bottomx, bottomy, hpagex, hpagey):
	return ImageGrab.grab((hpagex + topx, hpagey + topy, hpagex + bottomx, hpagey + bottomy))

def sleep(sec):
	time.sleep(sec + random.random())

def handle_page(hpagex, hpagey):
	refresh_handle_page()
	sleep(3)
	check_time = 0
	while check_time < 30:
		#情景1 图片尚未刷新成功
		#	   继续等待，30s后仍无变化，则刷新
		#情景2 没有选择物品 直接刷新
		# 确认摇杆已加载
		img_manage = pic_grab(70 + dict['click_x'], 197 + dict['click_y'], 266 + dict['click_x'], 448 + dict['click_y'], hpagex, hpagey)
		img_manage.save('roll_page.jpg')
		img_manage_hash = get_hash(img_manage)
		if not hamming(img_manage_hash, dict['roll_page_hash']):
			sleep(1)
			check_time += 1
			continue

		talk_pic = pic_grab(307 + dict['click_x'], 131 + dict['click_y'], 383 + dict['click_x'], 145 + dict['click_y'], hpagex, hpagey)
		talk_pic.save('talk_block.jpg')
		if hamming(get_hash(talk_pic), dict['block_hash']):
			#确认为情景2，前往点击CC auto
			print_to_file("当前没有物品被选择")
			return 2

		# 否则说明已加载好
		break
	if check_time == 30:
		print_to_file("如果确认roll_page.jpg为人物裙子，但仍然无法识别，请复制下方内容覆盖程序中dict['roll_page_hash']字符串\n" + img_manage_hash)
		# 刷新摇杆页面，并重新识别
		refresh_handle_page()
		return 1

	# 确认把手位置正确
	handle_img = pic_grab(277 + dict['click_x'], 281 + dict['click_y'], 304 + dict['click_x'], 310 + dict['click_y'], hpagex, hpagey)
	handle_img.save('handle.jpg')
	handle_img_hash = get_hash(handle_img)
	if not hamming(handle_img_hash, dict['handle_hash']):
		print_to_file("如果确认handle.jpg为把手，但仍然闪退，请复制下方内容覆盖程序中dict['handle_hash']字符串\n" + handle_img_hash)
		return -1

	# 把手无误，点击
	move_click(hpagex + random_in_range(277 + dict['click_x'], 304 + dict['click_x']), hpagey + random_in_range(281 + dict['click_y'], 310 + dict['click_y']))
	sleep(1)

	# 确认丢弃页面
	confirm_page = pic_grab(-185 + dict['no_x'], -163 + dict['no_y'], 236 + dict['no_x'], 21 + dict['no_y'], hpagex, hpagey)
	confirm_page.save('confirm_page.jpg')
	confirm_page_hash = get_hash(confirm_page)
	if not hamming(confirm_page_hash, dict['confirm_hash']):
		print_to_file("如果确认confirm_page.jpg为确认页面，但仍然闪退，请复制下方内容覆盖程序中dict['confirm_hash']字符串\n" + confirm_page_hash)
		return -1
	sleep(1)

	# 点击YES
	yes_btn = pic_grab(34 + dict['no_x'], -19 + dict['no_y'], 86 + dict['no_x'], 17 + dict['no_y'], hpagex, hpagey)
	yes_btn.save('yes_btn.jpg')
	yes_btn_hash = get_hash(yes_btn)
	if not hamming(yes_btn_hash, dict['yes_hash']):
		print_to_file("如果确认yes_btn.jpg为Yes按钮，但仍然闪退，请复制下方内容覆盖程序中dict['yes_hash']字符串\n" + yes_btn_hash)
		return -1

	move_click(hpagex + random_in_range(34 + dict['no_x'], 86 + dict['no_x']), hpagey + random_in_range(-19 + dict['no_y'], 17 + dict['no_y']))

	# 等待10s 提供充足等待时间
	sleep(10)

	# 确认捐赠成功
	success_page = pic_grab(2 + dict['click_x'], 624 + dict['click_y'], 276 + dict['click_x'], 722 + dict['click_y'], hpagex, hpagey)
	success_page.save('success.jpg')
	success_page_hash = get_hash(success_page)
	if not hamming(success_page_hash, dict['success_hash']):
		print_to_file("如果确认success.jpg为下方无可捐物品空白页面，但仍然闪退，请复制下方内容覆盖程序中dict['success_hash']字符串\n" + success_page_hash)
		return -1
	return 0

def refresh_handle_page():
	# 刷新摇杆页面
	refresh_btn = pic_grab(dict['refreshx'] -8, dict['refreshy'] - 8, dict['refreshx'] + 8, dict['refreshy'] + 8, hpagex, hpagey)
	refresh_btn.save('refresh.jpg')
	move_click(hpagex + random_in_range(dict['refreshx'] -8, dict['refreshx'] + 8), hpagey + random_in_range(dict['refreshy'] - 8, dict['refreshy'] + 8))

def inventory(ipagex, ipagey):
	# CC auto按钮
	cc_auto_btn = pic_grab(-18 + dict['cc_auto_x'], -16 + dict['cc_auto_y'], 157 + dict['cc_auto_x'], 16 + dict['cc_auto_y'], ipagex, ipagey)
	cc_auto_btn.save('cc_btn.jpg')
	cc_img_hash = get_hash(cc_auto_btn)
	if not hamming(cc_img_hash, dict['cc_btn_hash']):
		print_to_file("如果确认cc_btn.jpg为CC auto按钮，但仍然闪退，请复制下方内容覆盖程序中dict['cc_btn_hash']字符串\n" + cc_img_hash)
		return -1
	move_click(ipagex + random_in_range(-18 + dict['cc_auto_x'], 157 + dict['cc_auto_x']), ipagey + random_in_range(-16 + dict['cc_auto_y'], 16 + dict['cc_auto_y']))
	#静静等待破产插件运行吧
	sleep(90)

	check_time = 0
	while check_time < 10:
		# 获取猴子提示
		monkey_say = pic_grab(1503, 927, 1749, 1015, 0, 0)
		monkey_area_hash = get_hash(monkey_say)
		# monkey_say.save(str(check_time) + '_monkey_say_area.jpg')
		# print_to_file(str(check_time) + " " + monkey_area_hash)
		if not hamming(monkey_area_hash, dict['monkey_say_hash']):
			check_time += 1
			sleep(1)
			continue
		check_time = 233
		
	if check_time != 233:
		monkey_say.save('monkey_say_area.jpg')
		print_to_file("请检查Tampermonkey提示消息，是否在monkey_say_area.jpg截取位置出现")
		return -1
	return 0

def init_with_file():
	if not os.path.isfile("init.txt"):
		print_to_file("no file")
	else:
		with open('init.txt', encoding= 'gb18030', errors='ignore') as file:
			lines = file.readlines()
			for line in lines:
				if "=" not in line:
					continue
				line = line.replace(" ", "").replace("\'", "").replace("\"", "").replace("\n", "").replace("\r", "")
				pair = line.split("=")
				left = pair[0]
				right = pair[1]
				if "hash" not in line:
					right = int(right)
				if left in dict:
					dict[left] = right
				else:
					print_to_file("something wrong with the init.txt [Undefined Left Value] ------" + line)

def print_to_file(str):
	with open('log.txt', 'a+', encoding='utf8') as file:
		file.write(str + "\n")

if __name__ == '__main__':

	print_to_file("==================== Start Line ====================")
	# 从文件中读取数据
	init_with_file()
	
	# 初始化窗口定位
	handle_page_size = get_handle_page()
	item_page_size = get_item_page()
	if handle_page_size is not None and item_page_size is not None:
		hpagex = handle_page_size[0]
		hpagey = handle_page_size[1]

		ipagex = item_page_size[0]
		ipagey = item_page_size[1]

		while dict['item_num'] / 10:
			next_handle = handle_page(hpagex, hpagey)
			if next_handle == -1:
				exit(0)
			elif next_handle == 0:
				next_handle = inventory(ipagex, ipagey)
			elif next_handle == 2:
				next_handle = inventory(ipagex, ipagey)

			if next_handle == -1:
				exit(0)

			dict['item_num'] -= 10
		print_to_file("\nCongratulations!")
	else:
		print_to_file("请确认已分别打开物品柜页面和摇杆页面")

	print_to_file("===================== End Line =====================\n\n")