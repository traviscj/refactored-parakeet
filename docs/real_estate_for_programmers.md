
# Real Estate for Programmers

I hit the jackpot when I joined Square, and ended up with a bit of money to invest. 
For many years, I stuck to other stocks and ETFs, but I eventually went looking for something different.

I've been extremely fortunate to have a great partner (my wife) that let me drag us down this path!



## Properties

The most fundamental view of a property might be something like

    @dataclass
    class Property:
        address: str
        income: int
        noi: int
        expenses: typing.map[str, int]
