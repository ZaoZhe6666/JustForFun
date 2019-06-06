#coding:UTF-8
import win32api
import win32con
import win32gui
import time
import os
import random
from PIL import ImageGrab
from PIL import Image

o_x = 430
o_y = 162

start_hash = "2d80daff71ff60ff62ff03bf00dc50dcbf0d003cd620fd00ed00ed00ed3eef7e"
ground_hash = "800fffff7ff00006ffffffff000007de027c0028002000000800380000000000"
zero_hash = "fc01fc01fc01fc01fc01fc03fc03fc03fc07f807f80f001f003f00ffffffffff"
goal_hash = "e1ffe1ffe1ffe1ffe1fce1dee1ffe176c177c07ee1f6e03ee0b7e1b7e1e0e1fd"
finish_hash = "00000000000024000100fff7fffcff801e003c007800f9ecf180c22ac2000000"
restart_hash = "00000000c060c9407f1eff1ef67efe7ece3cff7e3d72fffefffe000000000000"

def move_click(x, y, t = 0):  # 移动鼠标并点击左键
	win32api.SetCursorPos((x, y))  # 设置鼠标位置(x, y)
	if t == 0:
		win32api.mouse_event(win32con.MOUSEEVENTF_LEFTDOWN | win32con.MOUSEEVENTF_LEFTUP, x, y, 0, 0)  # 点击鼠标左键

def space_click():
	win32api.keybd_event(32, 0, 0, 0)
	sleep(0)
	win32api.keybd_event(32, 0, win32con.KEYEVENTF_KEYUP, 0)

def get_handle_page():  # 获取把手页面窗口信息
	wdname = u'Neopets - Altador Cup - Games - Google Chrome'
	handle = win32gui.FindWindow(None, wdname)  # 获取窗口句柄
	if handle == 0:
		print_to_file("Error to find Page\" shootout showdown \"")
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

def print_to_file(str):
	with open('log.txt', 'a+', encoding='utf8') as file:
		file.write(str + "\n")

if __name__ == '__main__':

	# 初始化窗口定位
	game_page_size = get_handle_page()
	if game_page_size is not None:
		hpagex = game_page_size[0]
		hpagey = game_page_size[1]
		
		while 1:
			# 判断当前为开始游戏界面
			img = pic_grab(565, 627, 707, 668, o_x, o_y)
			img.save("start_game.jpg")
			hash_value = get_hash(img)
			if not hamming(hash_value, start_hash):
				print("start game")
				print(get_hash(img))
				exit(0)

			# 点击开始
			move_click(random_in_range(565, 707) + o_x, random_in_range(627, 668) + o_y)
			sleep(1)

			# 判断当前没进球
			# img = pic_grab(572, 631, 703, 720, o_x, o_y)
			# img.save("finish.jpg")
			# hash_value = get_hash(img)
			
			img = pic_grab(155, 615, 536, 686, o_x, o_y)
			img.save("score.jpg")
			hash_value = get_hash(img)

			while not hamming(hash_value, goal_hash):
				# 判断当前可踢球 - 踢球
				# img = pic_grab(171, 546, 546, 705, o_x, o_y)
				# img.save("block_ground.jpg")
				# hash_value = get_hash(img)
				# if hamming(hash_value, ground_hash)
				
				# 不判断了 踢进就停
				space_click()
				time.sleep(0.1)
				img = pic_grab(155, 615, 536, 686, o_x, o_y)
				img.save("score.jpg")
				hash_value = get_hash(img)
			sleep(2)
			# 点击结束踢球
			move_click(random_in_range(589, 708) + o_x, random_in_range(66, 86) + o_y)
			sleep(2)

			# 点击发送分数
			move_click(random_in_range(576, 703) + o_x, random_in_range(692, 720) + o_y)
			sleep(5)

			# 重新开始
			img = pic_grab(463, 204, 582, 225, o_x, o_y)
			img.save("restart.jpg")
			hash_value = get_hash(img)
			if hamming(hash_value, restart_hash):
				move_click(random_in_range(463, 573) + o_x, random_in_range(204, 225) + o_y)
			else:
				sleep(5)
				if hamming(hash_value, restart_hash):
					move_click(random_in_range(463, 573) + o_x, random_in_range(204, 225) + o_y)
				else:
					print("Not Restart Page")
					print(get_hash(img))
					exit(0)
			sleep(2)