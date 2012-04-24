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
 define(['light/enums/PagesEnum', 'lightTest/TestUtils'],
        function(PagesEnum, TestUtils) {
   var SAMPLE_UNKOWN_PATH = 'SAMPLE_UNKOWN_PATH';

   var enumNames = TestUtils.getEnumNames(PagesEnum);

   describe('light.enums.PagesEnum', function() {

    describe('count', function() {
      it('should be ...', function() {
        expect(enumNames.length).toBe(3);
      });
    });

    describe('values', function() {
      it('should be base on the name', function() {
        for (var i = 0, len = enumNames.length; i < len; i++) {
          var pageName = enumNames[i];
          var page = PagesEnum[enumNames[i]];
          expect(page.path).toBe('/' + pageName.toLowerCase());
          expect(page.build).toBe(pageName.toLowerCase());
          expect(page.main.toLowerCase()).toBe(pageName.toLowerCase() + 'main');
          TestUtils.expectModuleToExist('light/main/' + page.main);
        }
      });
    });

    describe('events default', function() {
      it('should be valid', function() {
        for (var i = 0, leni = enumNames.length; i < leni; i++) {
          var page = PagesEnum[enumNames[i]];
          for (var j = 0, lenj = page.states.length; j < lenj; j++) {
            new page.states[j].Builder().build();
          }
        }
      });
    });



    describe('getByPath', function() {
      describe('when called with a known path', function() {
        it('should map to the correct enum value', function() {
          for (var i = 0, len = enumNames.length; i < len; i++) {
            var page = PagesEnum[enumNames[i]];
            expect(PagesEnum.getByPath(page.path)).toBe(page);
          }
        });
      });

      describe('when called with a unknown path', function() {
        it('should return null', function() {
          expect(PagesEnum.getByPath(SAMPLE_UNKOWN_PATH)).toBe(null);
        });
      });
    });
  });
 });
