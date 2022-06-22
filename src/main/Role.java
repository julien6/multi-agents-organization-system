package main;

import java.util.Arrays;
import java.util.List;

public class Role {

	public static Role r0 = new Role(Arrays.asList(2, 2), "r0");
	public static Role r1 = new Role(Arrays.asList(25, 40), "r1");
	public static Role r2 = new Role(Arrays.asList(15, 30), "r2");
	public static Role r3 = new Role(Arrays.asList(35, 20), "r3");

	public static List<Role> roles = Arrays.asList(r0, r1, r2, r3);

	public List<Integer> roleVector;
	public String roleName;

	public Role(List<Integer> roleVector, String roleName) {
		this.roleVector = roleVector;
		this.roleName = roleName;
	}

	@Override
	public String toString() {
		return this.roleName + "(" + this.roleVector.toString() + ")";
	}

}
