/*
 * Copyright (C) Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * Calling window.define instead of define because of dojo build bug.
 * @see http://bugs.dojotoolkit.org/ticket/15057
 */
window.define({
  searchButtonLabel: 'Pesquisar',
  didYouMeanBox: 'Você quis dizer ${suggestion}?',
  noResults: 'Sua pesquisa não retornou resultados',
  pageInfo: 'Página ${page}'
});