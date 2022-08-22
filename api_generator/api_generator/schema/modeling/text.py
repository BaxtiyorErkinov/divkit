from __future__ import annotations
from typing import Optional, Union, List
from ..utils import is_list_of_type
from ...utils import indented


class Text:
    def __init__(self, init_lines: Optional[Union[str, List[str]]] = None, indent_width: int = 0):
        self._lines = []
        self._indent_width = indent_width
        if init_lines is not None:
            if isinstance(init_lines, str):
                self._lines.append(init_lines)
            elif is_list_of_type(init_lines, str):
                self._lines.extend(init_lines)
            else:
                raise TypeError

    def __str__(self):
        def transform(string: str) -> str:
            if not string or string.isspace():
                return ''
            else:
                return indented(string, indent_width=self.indent_width)
        return '\n'.join(map(transform, self._lines))

    def __add__(self, other) -> Text:
        if isinstance(other, str):
            return Text(init_lines=self._lines + [other], indent_width=self.indent_width)
        elif is_list_of_type(other, str):
            return Text(init_lines=self._lines + other, indent_width=self.indent_width)
        elif is_list_of_type(other, Text):
            sum_lines = self._lines.copy()
            for other_text in other:
                sum_lines.append(str(other_text))
            return Text(init_lines=sum_lines, indent_width=self.indent_width)
        elif isinstance(other, Text):
            return Text(init_lines=self._lines + [str(other)], indent_width=self.indent_width)
        else:
            raise TypeError

    def indented(self, level: int = 1, indent_width: int = 2) -> Text:
        return Text(init_lines=self._lines, indent_width=self._indent_width + level * indent_width)

    @property
    def indent_width(self) -> int:
        return self._indent_width

    @property
    def lines(self) -> List[str]:
        return self._lines


EMPTY: Text = Text('')
