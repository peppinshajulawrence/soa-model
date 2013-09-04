/* Copyright 2012 predic8 GmbH, www.predic8.com
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. */

package com.predic8.schema

import com.predic8.soamodel.*

class SchemaValidator {

	void validate(Schema schema, AbstractParserContext ctx) {
		validateElements(schema.elements, schema, ctx)
		validateComplexTypes(schema.complexTypes, schema, ctx)
		validateSimpleTypes(schema.simpleTypes, schema, ctx)
	}

	void validateElements(elements, schema, ctx) {
		elements.each {
			if(it.type) {
				try {
					if(!schema.getType(it.type)) {
						ctx.errors << new ValidationError(invalidElement : it, message : "Element ${it.name} uses '${it.type}' as its type, which is not defined in this schema.")
					}
				} catch (Exception e) {
					e.printStackTrace();
					ctx.errors << new ValidationError(invalidElement : it, message : "Element ${it.name} is invalid!")
				}
			}
			else if(it.ref) {
				try {
					if(!schema.getElement(it.ref)) {
						ctx.errors << new ValidationError(invalidElement : it, message : "Some element in this document uses '${it.ref}' as its reference, which is not defined in this schema.")
					}
				} catch (Exception e) {
					ctx.errors << new ValidationError(invalidElement : it, message : "Element with ref '${it.ref}' is invalid!")
				}
			}
		}
	}

	void validateComplexTypes(complexTypes, schema, ctx) {
		complexTypes.each { ct ->
			if(ct.superTypes) {
				ct.superTypes.each {
					if(!schema.getType(it)) {
						ctx.errors << new ValidationError(invalidElement : ct, message : "ComplexType ${ct.name} inherits from '${it}', which is not definded in this schema.")
					}
				}
			}
			
			//TODO Validating Attributes has to be refactored. Otherwise tests fail!
//			if(ct.allAttributes){
//				ct.allAttributes.each {attr ->
//					try {
//						//An attribute should have either a ref or a type.
//						if(ct.schema.getAttribute(attr.ref) || ct.schema.getType(attr.type)) { return }
//						ctx.errors << new ValidationError(invalidElement : ct, message : "ComplexType ${ct.name} defines an attribute, which is not valid in this schema.")
//					} catch (Exception e) {
//						ctx.errors << new ValidationError(invalidElement : ct, message : "ComplexType ${ct.name} defines an attribute, which is not valid in this schema.")
//					}
//				}
//			}
			
			if(ct.model?.hasProperty("particles")) validateElements(ct.model.particles.grep(Element), ct.schema, ctx)
		}
	}

	void validateSimpleTypes(simpleTypes, schema, ctx) {
		simpleTypes.each { st ->
			if(st.superTypes) {
				st.superTypes.each {
					if(!schema.getType(it)) {
						ctx.errors << new ValidationError(invalidElement : st, message : "SimpleType ${st.name} inherits from '${it}', which is not definded in this schema.")
					}
				}
			}
		}
	}
}