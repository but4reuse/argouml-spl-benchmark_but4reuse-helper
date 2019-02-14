package org.but4reuse.benchmarks.argoumlspl.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Trace Id utils
 * 
 * @author jabier.martinez
 */
public class TraceIdUtils {

	public static String getId(PackageDeclaration packageDeclaration) {
		if (packageDeclaration == null) {
			return "defaultPackage";
		}
		return packageDeclaration.getName().getFullyQualifiedName();
	}

	public static String getId(TypeDeclaration typeDeclaration) {
		CompilationUnit cu = (CompilationUnit) typeDeclaration.getRoot();
		return getId(cu.getPackage()) + "." + typeDeclaration.getName().getFullyQualifiedName();
	}

	public static String getId(MethodDeclaration node) {
		StringBuffer qname = new StringBuffer();
		// TODO handle AnonymousClassDeclaration
		if (node.getParent() instanceof TypeDeclaration) {
			qname.append(getId((TypeDeclaration) node.getParent()));
			qname.append(" ");
			qname.append(node.getName().getIdentifier());
			// add parameters to signature
			qname.append("(");
			List<?> parameters = node.parameters();
			for (Object parameter : parameters) {
				if (parameter instanceof SingleVariableDeclaration) {
					qname.append(((SingleVariableDeclaration) parameter).getType());
					qname.append(",");
				}
			}
			// remove last comma
			if (!parameters.isEmpty()) {
				qname.setLength(qname.length() - 1);
			}
			qname.append(")");
		} else {
			System.err.println("IdUtils: Not handled");
		}
		return qname.toString();
	}
}
