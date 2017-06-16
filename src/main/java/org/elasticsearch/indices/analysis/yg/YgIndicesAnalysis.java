/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.indices.analysis.yg;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.YgUtil;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.*;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;

/**
 * Registers indices level analysis components so, if not explicitly configured,
 * will be shared among all indices.
 */
public class YgIndicesAnalysis extends AbstractComponent {

	@Inject
	public YgIndicesAnalysis(Settings settings, IndicesAnalysisService indicesAnalysisService) {
		super(settings);

		// Register yg analyzer
		indicesAnalysisService.analyzerProviderFactories().put("yg",
				new PreBuiltAnalyzerProviderFactory("yg", AnalyzerScope.INDICES, YgUtil.getYgAnalyzer(settings)));

		// Register yg_tokenizer tokenizer
		indicesAnalysisService.tokenizerFactories().put("yg_tokenizer",
				new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
					@Override
					public String name() {
						return "yg_tokenizer";
					}

					@Override
					public Tokenizer create() {
						return YgUtil.getYgTokenizer(settings);
					}
				}));
	}
}
