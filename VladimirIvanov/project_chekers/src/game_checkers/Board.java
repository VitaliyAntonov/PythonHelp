/**
 * 
 */
package game_checkers;

import java.util.Iterator;
import java.util.Vector;
/**
 * @author vin
 *
 */
public class Board {

    Vector<PosAndValue> checkers_in_fire;
    
	public String[][] board;
	Boolean motion;
	
	PosAndValue curr_enemy_coord;
	PosAndValue cell_destination_after_change = new PosAndValue("", 0, 0);
	PosAndValue cell_from;
	Boolean use_curr_enemy_coord = false;
	String s_next_way_attack; //��������-�������� ��� ���������� ���������� ���� ��� ������������ �����

	Boolean step_was_succeess = false;
	Boolean flag_dont_changed = false; //��������� �������� ����� ���� (23.03)

	Boolean active_figure_allow = true;

	public PosAndValue cell_after_enemy = new PosAndValue("", 0, 0); //�������� ������ �� ��������� �������
	public Way way_possible_attak;
	public Vector<Way> fireways_curr_pos; //16.03
	public Vector<Way> simpleways_curr_pos; //16.03 //���� ���� ������ �����, ��� ���������
	
	Step previous_success_step = new Step("");
	Boolean last_step_was_attack = true;

	Boolean need_change_motion_after_step; //���������������� check_double_attack(String)
	Vector<Step> vstep = new Vector<Step>(); //������� �����

	Boolean patry_not_first = false;
	Vector<Way> vw_possible_way_next_attak = new Vector<Way>(); //���� ����� ����� ����� ���������� ��� �����, ��� ���������� ����

 	Vector<Way> vpw_fire = new Vector<Way>();
    Vector<Way> vpw_free = new Vector<Way>();
    Vector<Way> vpw_both = new Vector<Way>();
    
    Boolean end_of_game = false; // ��������� ��������� ����. ���� ������ - ���� �������� (23.03)
	
 
//������� ��������� ������� � ������ �����    
    public void clear_vpw_fire_and_free() {
    	vpw_fire.clear();
    	vpw_free.clear();
    	vpw_both.clear();
    }
    
    public void define_vector_any_ways() {
	    SetPossibleWays spw = new SetPossibleWays(this);
	    if(vw_possible_way_next_attak.size() > 0) {
	    	vpw_fire.clear();
	    	vpw_fire.addAll(vw_possible_way_next_attak);
	    	vpw_free.clear();
	    } else {
	     	vpw_fire = spw.get_vpw_fire();        
	        vpw_free = spw.get_vpw_free();
	    }
        vpw_both.addAll(vpw_fire);
        vpw_both.addAll(vpw_free);
	    System.out.println(toPrintFireAndFree());
    }
    
/** ����� ������ ��� � ����, ��� ������ ������, ��� ��� ����� �� �������� - ������, �������� */    
    public Boolean there_is_no_steps() {
    	if(vpw_both.size() == 0 || get_set_current_army().size() == 0) {
    		report_end_game();
    		end_of_game = true;
    	} else {
    		end_of_game = false;
    	}
    	return end_of_game;
    }

/** ����� ����� ����� ����*/    
    public void reset_end_of_game() {
    	end_of_game = false;
    }
    
/** ����� ��������� �� ����� � ����������� ���� */    
    public void report_end_game() {
    	if(motion) {
    		System.out.println("Black win");
    	} else {
    		System.out.println("White win");
    	}
    }

    
	public Boolean flag_dont_changed() {
		return flag_dont_changed; //15.03
	}
	
	public Board (String[][] board) {
		this.board = board;
		motion = true;//��� �����
	}

	public Boolean get_motion(){
		return motion;
	}	
	
	public Boolean change_motion(){
		motion = !motion;
		remove_flag_not_changed();
		return motion;
	}

	
	public Boolean is_enemy(String checkers) {
		Boolean res = false;
		if(Static.is_clear(checkers)) {
			return false;
		}
		
		if(motion) { //�����
			if(checkers == Static.w_e || checkers == Static.w_d) {
				return false;
			} else {
				if((checkers == Static.b_e || checkers == Static.b_d)) {
			    return true;
			}
			}
		} else {     //������
			if(checkers == Static.b_e || checkers == Static.b_d) {
				return false;
			} else {
				if((checkers == Static.w_e || checkers == Static.w_d)) {
			    return true;
			}
			}		
		} 
		return res;
	}
	
	public Boolean is_friend(String checkers) {
		if(Static.is_clear(checkers) == false && is_enemy(checkers) == false) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean possible_attack_stat(Vector<PosAndValue> vpnv) {
		Boolean possibility_attak = false;
		Iterator<PosAndValue> i = vpnv.iterator();
		int counter_enemy = 0;
		int counter_friend = 0;
        String checkers;
        PosAndValue pnv;
        String first_chekers = vpnv.firstElement().get_value();
        String last_chekers = vpnv.lastElement().get_value();
        if(is_friend(first_chekers) 
        && Static.is_clear(last_chekers)) {
			while(i.hasNext()) {
				pnv = i.next();
				checkers = pnv.get_value();
				if(is_enemy(checkers)) {
					counter_enemy++;
					possibility_attak = true;
				};
				if(is_friend(checkers)) {
					counter_friend++;
				};
			}
			if(counter_enemy == 1 && counter_friend == 1) {
				possibility_attak = true;
			} else {
				possibility_attak = false;
			}
			
			if(Static.isSimpleCheckers(first_chekers)) {
				if(vpnv.size() != 3) {
					possibility_attak = false; //����������� ��� �����
				}
			}
        }
        return possibility_attak; //���������� ����������� �����		
	}
	
	public Boolean possible_attack(Vector<PosAndValue> vpnv) {
		Iterator<PosAndValue> i = vpnv.iterator();
		int counter_enemy = 0;
		int counter_friend = 0;
        String checkers;
        PosAndValue pnv;
        String first_chekers = vpnv.firstElement().get_value();
        String last_chekers = vpnv.lastElement().get_value();
        use_curr_enemy_coord = false;
        Boolean enemy_detected = false;
        cell_after_enemy.clear_value();
        if(is_friend(first_chekers) 
        && Static.is_clear(last_chekers)) {
			while(i.hasNext()) {
				pnv = i.next();
				checkers = pnv.get_value();
				if(is_enemy(checkers)) {
					counter_enemy++;
					curr_enemy_coord = pnv;
					use_curr_enemy_coord = true;
					enemy_detected = true;
				};
				if(is_friend(checkers)) {
					counter_friend++;
				};
				if(enemy_detected && pnv.get_value() == Static.sp2) {
					//���� ��������� �� ��������� ������
					//�� ��� ������, ��� �� ������� ����� �������
					cell_after_enemy = pnv;
				}
			}
			if(counter_enemy == 1 && counter_friend == 1 && cell_after_enemy.active()) {
				use_curr_enemy_coord = true;
				way_possible_attak = new Way(vpnv.firstElement(),cell_after_enemy);
			} else {
				use_curr_enemy_coord = false;
			}
			
			if(Static.isSimpleCheckers(first_chekers)) {
				if(vpnv.size() != 3) {
					use_curr_enemy_coord = false; //����������� ��� �����
				}
			}
        }
        return use_curr_enemy_coord; //���������� ����������� �����
	}
	
	public Boolean step_was_attak() {
		return use_curr_enemy_coord; 
	}

//��� �� ���������� ��� ������	
	public Boolean previousStepWasAttack() {
		return previous_success_step.thisStepWasAttack();
	}
	
	public Boolean is_clear_move(Vector<PosAndValue> vpnv) {//16.03
		if(vpnv.firstElement().get_value() == Static.sp2 || vpnv.size() <= 1) {
			return false; //�� ��������� ������ � ������ �������, � ���� �������� ����, ��� ���� ����� �� ����������
		}
		if(vpnv.lastElement().get_value() != Static.sp2) {//���� ����� ���������� �������, ��� ����������
			return false;
		}
		if(vpnv.firstElement().get_value() == Static.w_e && motion == true ||
		   vpnv.firstElement().get_value() == Static.b_e && motion == false ) {
			if(vpnv.size() == 2) {
				return true;
			} else {
				return false;
			}
		} else {			
			if (vpnv.firstElement().get_value() == Static.w_d && motion == true ||
			   vpnv.firstElement().get_value() == Static.b_d && motion == false ) {
				Iterator<PosAndValue> i = vpnv.iterator();
				if(i.hasNext()) { //���������� ������ ������� - ���� ������ �����
					i.next();
				}
				while(i.hasNext()) { //���� ��� ���� ������
					if(Static.isNotClearField(i.next().get_value()))
						return false;
				}
				return true;
			} else return false;	
		}
	}
	

	
	public Boolean mark_enemy() {
		if(use_curr_enemy_coord) {
			change_cell_x_y(curr_enemy_coord.get_x(),curr_enemy_coord.get_y(),Static.sp2);
			checkers_in_fire.add(curr_enemy_coord);
			use_curr_enemy_coord = false;
			return true;
		} else {
			return false;			
		}	
	}
	
	public void remove_enemy() {
		if(checkers_in_fire != null) {
			checkers_in_fire.removeAllElements();			
		}
	}
 
	public String boardToConsole(){
		String s = "";
	    String sep = "|";
	     for(int i=0; i < board.length; i++) {
	    	 for(int j=0; j < board[0].length; j++){
	    		 s = s.concat(board[i][j]);
	    	 }
	    	 s = s.concat(sep+"\n");
	     }
		return s;
	}
	
		
		public void change_cell_x_y(int x, int y, String argument) {
			board[y][x] = argument;
			}
		
		public String get_cell_x_y(int x, int y) {
			return board[y][x];
			}		
			

		public PosAndValue getCoordinatesEnemy() {
			return curr_enemy_coord;
		}
		
		public Vector<PosAndValue> get_massive_figure(String step) {
			Way way = new Way(step);
			Vector<PosAndValue> vpnv = way.get_massive_figure(this);
			return vpnv;
		}
		
		/**����� �� �� ������*/
		public static Boolean isSimpleFirstCheckers(Vector<PosAndValue> vpnv) {
			return Static.isSimpleCheckers(vpnv.firstElement().get_value());
		}

		public Boolean is_coorrect_fire_restriction(String step) {
			if(vw_possible_way_next_attak.size() > 0) {
				Way way = new Way(step);
				if(way.allowed_by_vector_attack(vw_possible_way_next_attak)) {
					need_change_motion_after_step = false;
					System.out.println("this step is double-step");
					return true; //step ����� ���������� ��� ���������� �����(��� �� ������/������)
				} else {
					need_change_motion_after_step = false; //��� ����� ����������, ������� ���� ��������
					return false; //��� ������� ���� ������ ��� ��� �� ������, � ���� step �� ����� ����������
					}
			} else {
				return true;
			}
		}
		
		
		/**������� ���������: ������� ���.
		 * ����� �������� ���������, ��� ��� step � ������ �����*/
		public void make_step(String step) {
			Vector<PosAndValue> vpnv = get_massive_figure(step);
			PosAndValue pnv_active_element = vpnv.firstElement();
			Way way = new Way(step);
			if(way.allow_use_way_for_step() && is_destination_clear(way)) {
				if(allow_color(pnv_active_element)){
					PosAndValue pnv_active_move = vpnv.lastElement();
					if(possible_attack(vpnv)){
						execute_attack();
						last_step_was_attack = true;
						simple_move(pnv_active_element,pnv_active_move);
						
					};
					if(is_clear_move(vpnv)) {
						if(isSimpleFirstCheckers(vpnv)) {
							if(vpnv.size() == 2 &&	way.accessible_dir(pnv_active_element.get_value())) {
								simple_move(pnv_active_element,pnv_active_move);
								last_step_was_attack = false;
							} else {
								step_was_succeess = false;
							}
						} else { //��� ��� ����� �����, � ��� ��������� ����������� ���������
							simple_move(pnv_active_element,pnv_active_move);
							last_step_was_attack = false;
						}
					}
					cell_destination_after_change = pnv_active_move;
					cell_destination_after_change.setValue(pnv_active_element.get_value());
					make_damka_if_pos(cell_destination_after_change);
				} else {
//4					System.out.println("This color unaccess");
					step_was_succeess = false;
				}			
			} else {
				step_was_succeess = false;
//5				System.out.println("This step unreal");
			}
			if(step_was_succeess) {
				previous_success_step = new Step(step);
				if(last_step_was_attack) {
					previous_success_step.markStepAsAttack();
				}
				vstep.add(previous_success_step);
				patry_not_first = true;
			}
			}

		
/** ��������, ��� ����� ���, ��� ��� */		
		private Boolean allow_color(PosAndValue pnv) {
			// TODO Auto-generated method stub
			if(motion) {
				if(pnv.value == Static.w_e || pnv.value == Static.w_d) {
					return true;
				} else {
					return false;
				}
			} else {
				if(pnv.value == Static.b_e || pnv.value == Static.b_d) {
					return true;
				} else {
					return false;
				}				
			}
		}

		/**
		 * ���� ���������� �� ��������������� ������, ������ ������
		 * */		
		private void make_damka_if_pos(PosAndValue pnv_active_element) {
			if(pnv_active_element.value == Static.w_e && pnv_active_element.get_y() == Static.y_white_limit) {
				change_cell_x_y(pnv_active_element.get_x(), pnv_active_element.get_y(), Static.w_d);
			}
			if(pnv_active_element.value == Static.b_e && pnv_active_element.get_y() == Static.y_black_limit) {
				change_cell_x_y(pnv_active_element.get_x(), pnv_active_element.get_y(), Static.b_d);
			}		
		}
		
		private void execute_attack() {
			change_cell_x_y(curr_enemy_coord.get_x(),curr_enemy_coord.get_y(), Static.sp2);
			remove_enemy();
		}

		public void simple_move(PosAndValue p_from, PosAndValue p_to){
			String active_elem = get_cell_x_y(p_from.get_x(), p_from.get_y());
			change_cell_x_y(p_to.get_x(), p_to.get_y(), active_elem);
			change_cell_x_y(p_from.get_x(), p_from.get_y(), Static.sp2);
			cell_destination_after_change = new PosAndValue(p_from.get_value(), p_to.get_x(), p_to.get_y());
			cell_from = new PosAndValue(p_from.get_value(), p_from.get_x(), p_from.get_y());
			step_was_succeess = true;
		}
		
public Boolean next_attack_if_possible() {
	Coordinates active = new Coordinates(cell_destination_after_change.get_x(),cell_destination_after_change.get_y());
	Iterator<Way> iways = active.getWays().iterator();
	while(iways.hasNext()) {
		if(possible_attack(iways.next().get_massive_figure(this))) {
			s_next_way_attack = curr_enemy_coord.toNameCell();
			return true; //����� ����� �������� �� curr_enemy_coord 
		}
	}
	s_next_way_attack = "";
	return false;
}

		
/**
 * �������� ��������� ��� ��������� ����� ��� ��� �� �����, ���� �� ��������*/
public String get_next_way_attack() {
	return s_next_way_attack;
}

public String  step_anounce() {
	// TODO Auto-generated method stub
	if(motion) {
		return "Step for White";
	} else {
		return "Step for Black";
	}

}

public Boolean step_was_succeess() {
	return step_was_succeess;
}

public void alert_step_error() {
	step_was_succeess = false;
}

public Boolean is_destination_clear(Way way) {
	return get_cell_x_y(way.get_last_coord().get_x(), way.get_last_coord().get_y()) == Static.sp2;
}

//��������� ����, ��� ����� ���� ���������
public void set_flag_not_changed() {
	flag_dont_changed = true;
}

//����� ���������� ����� �������� ����
public void remove_flag_not_changed() {
	flag_dont_changed = false;
}

public Way get_way_possible_attak() {
	return way_possible_attak;
}

//�������� ��������� ����� �������, ������� ������ ���� � ������ ������
public Vector<PosAndValue> get_set_current_army() {
	Vector<PosAndValue> vpv = toPosAndValues();
	Vector<PosAndValue> res = new Vector<PosAndValue>();
	Iterator<PosAndValue> ipnv = vpv.iterator();
	PosAndValue pnv;
	while(ipnv.hasNext()) {
		pnv = ipnv.next();
		if(allow_color(pnv)) {
			res.add(pnv);
		}
	}
	return res;
}

//��������� ��� ������ � ��������� �������
private Vector<PosAndValue> toPosAndValues() {
	Vector<PosAndValue> vpnv = new Vector<PosAndValue>(); 
	for(int i = 0;i< board.length; i++) {
		for(int j = 0;j< board[0].length; j++) {
			vpnv.add(new PosAndValue(board[i][j], j, i));
		}
	}
	return vpnv;
}

public String get_cell_x_y(Coordinates a) {
	return get_cell_x_y(a.get_x(), a.get_y());
}
/**
 * 	//���������: ������� ����� ���� ����������, � ��� �� ����� ����� ������� ������� �� ����.
	//���� ���� ��������� �������, ��������� ������������ ��� ���� ������ ��� �����
	//��� ����, ����� ���� �������� � ���, ��� ������ �����
	 * ���������� ���������, �������� �� ��������. �.�. �������� �� ��� step
 * */
public void check_double_attack(String step) {
//��� ���� ��� step - ��� ����� ���, ������� ����� ������� �����
//	previous_success_step - ��� ���������� ���, ������� ������ ������� ����
//	� get_random_step_new(Board) ����� ���� ������, ���� �� ���������� �� ������� ���, 
//	� ���������� ���� ����� ������ ��������, � ��������� ������� - ��� ����������� ����� ������
	
//����� �������, ������������� ����� ���� ����� ������, ���� ��� ���������, 
//	� � ����������� ����������� ���� ������� ���� ���� ������
	if(last_step_was_attack) {
		Coordinates a;
		a = previous_success_step.getSecondPartStep();
		PossibleWays pw = new PossibleWays(a, this);
		Vector<Way> p_ways_second_attack = pw.get_way_attack();
		this.vw_possible_way_next_attak = p_ways_second_attack;
		if(p_ways_second_attack.size() > 0) {
     		need_change_motion_after_step = false;//����������� ����� ��������
		} else {
			//����� ����� � ����� ���. ������, �������� ���� ����� ���, � ������ ��������� ������� ��������, ����� ���� ���������
			need_change_motion_after_step = true;
		}
	} else {
		need_change_motion_after_step = true;
	}
}

public Boolean is_need_change_motion() {
	return need_change_motion_after_step;
}

//** ����� ������� ����� � ������ */
public String printHistory() {
	Iterator<Step> i = vstep.iterator();
	String s = "";
	while(i.hasNext()) {
		s = s.concat(i.next().getNameStep())+"\n";
	}
	return s;
}

public String printHistoryLine() {
	// ������ "g3-h4,f6-e5,h4-g5,e5-d4,e3-c5,d6-b4,c3-a5,h6-f4,f2-g3,f4-e3,d2-f4,b6-c5,g3-h4,e7-d6,f4-g5,c5-d4,b2-c3,d4-b2,a1-c3,d6-c5,c3-b4,c7-b6,b4-d6,b6-c5,d6-b4,d8-c7,h2-g3,f8-e7,c1-b2,g7-f6,g3-f4,f6-e5,f4-d6,d6-f8,c7-d6,f8-c5,h8-g7,c5-e7,g7-h6,g1-h2,h6-f4,e7-f6,f4-g3,h4-f2,a7-b6,a5-c7,b8-d6,h2-g3,d6-e5,f6-d4";
	Iterator<Step> i = vstep.iterator();
	String s = "History steps: ";
	while(i.hasNext()) {
		s = s.concat(i.next().getNameStep())+",";
	}
	if(s.length() > 0) {
		s = s.substring(0, s.length() - 1);
	}
	return s;
}

public String toPrintFireAndFree() {
	 return "available steps attack :"+ Static.VectorWayToPrint(vpw_fire) + "\n" +
			"available simple steps :"+ Static.VectorWayToPrint(vpw_free) + "\n";
}


}
